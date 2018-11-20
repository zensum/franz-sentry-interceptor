package se.zensum

import franz.WorkerBuilder
import franz.engine.mock.MockConsumerActor
import franz.engine.mock.MockMessage
import kotlinx.coroutines.runBlocking
import se.zensum.franzSentry.SentryInterceptor

fun main(args: Array<String>){
    runBlocking{
        createWorker().start()
    }

    Thread.sleep(2000)
}

suspend fun createWorker() =
    WorkerBuilder.ofString
        .subscribedTo("dummytopic")
        .setEngine(MockConsumerActor.ofString(
            listOf(MockMessage(topic = "dummy", value = "dummy"))
        ).createFactory())
        .install(SentryInterceptor(
            dsn = "https://9e1f89ff2b334cf9859d9b718df879b7:27cdfb0000324137b988fc218bb06288@sentry.io/1253981",
            appEnv = "sandbox"
        )).handlePiped {
        it
            .map(msg = "Mapping to X") { throw Exception("dummy") }
            .end()
    }