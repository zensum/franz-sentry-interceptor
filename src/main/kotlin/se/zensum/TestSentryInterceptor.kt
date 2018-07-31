package se.zensum.franzSentry

import franz.JobStatus
import franz.WorkerInterceptor
import mu.KotlinLogging

private val log = KotlinLogging.logger("sentry test")

/* Used in place of SentryInterceptor for test cases. Works the same on the surface but only logs the execeptions
 * and never sent anything to sentry
 */
class TestSentryInterceptor(
    onIntercept: suspend (interceptor: WorkerInterceptor, default: JobStatus) -> JobStatus = {interceptor, default ->
        try{
            interceptor.executeNext(default)
        }catch (e: Throwable){
            log.info { e.message }
            throw e
        }
    }
): WorkerInterceptor( onIntercept = onIntercept)