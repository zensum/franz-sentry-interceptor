package se.zensum.franzSentry

import franz.JobState
import franz.JobStateException
import franz.JobStatus
import franz.WorkerInterceptor
import franz.engine.kafka_one.KafkaMessage
import io.sentry.Sentry
import io.sentry.event.Breadcrumb
import io.sentry.event.BreadcrumbBuilder
import io.sentry.event.Event
import io.sentry.event.EventBuilder
import io.sentry.event.interfaces.ExceptionInterface
import mu.KotlinLogging
import se.zensum.toJson
import java.util.*

fun getEnv(e : String, default: String? = null) : String = System.getenv()[e] ?: default ?: throw RuntimeException("Missing environment variable $e and no default value is given.")

private val log = KotlinLogging.logger("sentry")

private fun getContextContent(value: Any?) = when(value){
    is KafkaMessage<*,*> -> value.value()
    else -> value
}

class SentryInterceptor(
    dsn: String? = getEnv("SENTRY_DSN", null),
    appEnv: String? = getEnv("APP_ENV", ""),
    onIntercept: suspend (interceptor: WorkerInterceptor, default: JobStatus) -> JobStatus = { interceptor, default -> try{
            interceptor.executeNext(default)
        }catch (e: JobStateException){
            val event = EventBuilder()
                .withMessage("Exception caught")
                .withLevel(Event.Level.ERROR)
                .withSentryInterface(ExceptionInterface(e))
                .withExtra("input", getContextContent(interceptor.jobState?.context?.first()?.input))
                .withBreadcrumbs(interceptor.jobState?.breadcrumbs?.map {
                    BreadcrumbBuilder().setMessage(it).build()
                })

            Sentry.capture(event)
            throw e
        } catch (e: Throwable){
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

    private fun sentryWrapper(interceptor: WorkerInterceptor, default: JobStatus): JobStatus{
        return JobStatus.Retry
    }
}
