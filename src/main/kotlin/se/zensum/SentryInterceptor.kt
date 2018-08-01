package se.zensum.franzSentry

import franz.JobStateException
import franz.JobStatus
import franz.WorkerInterceptor
import io.sentry.Sentry
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface
import mu.KotlinLogging

fun getEnv(e : String, default: String? = null) : String = System.getenv()[e] ?: default ?: throw RuntimeException("Missing environment variable $e and no default value is given.")

private val log = KotlinLogging.logger("sentry")

class SentryInterceptor(
    dsn: String? = getEnv("SENTRY_DSN", null),
    appEnv: String? = getEnv("APP_ENV", ""),
    onIntercept: suspend (interceptor: WorkerInterceptor, default: JobStatus) -> JobStatus = {interceptor, default ->
        log.info { "onIntercept invoked" }
        try{
            interceptor.executeNext(default)
        }catch (e: JobStateException){
            log.info { "Sentry interception: ${e.message}" }
            Sentry.capture(EventBuilder()
                .withMessage("Exception caught")
                .withLevel(Event.Level.ERROR)
                .withSentryInterface(ExceptionInterface(e)))
            throw e
        } catch (e: Throwable){
            log.info { "Sentry interception: ${e.message}" }
            Sentry.capture(e)
            throw e
        }finally {
            Sentry.clearContext()
        }
    }
): WorkerInterceptor( onIntercept = onIntercept){

    init{
        log.info { "Setting up Sentry Interceptor" }
        try {
            check(dsn != null) { "Sentry DSN must be set for sentry to work" }

            Sentry.init(dsn)
            Sentry.getStoredClient().apply {
                val user: String? = System.getProperty("user.name")
                this.serverName = user?.let { "$it@" } + hostname()

                appEnv?.let {
                    this.environment = appEnv
                }
            }
        }catch (e: Throwable){
            log.info { "Failed to set up Sentry Interceptor: ${e.message}" }
        }
    }
}
