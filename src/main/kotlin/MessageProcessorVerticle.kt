package com.example

import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class MessageProcessorVerticle : AbstractVerticle() {

    override fun start(startPromise: Promise<Void>) {
        CoroutineScope(vertx.dispatcher()).launch {
            try {
                // Register consumer for AMPS messages
                vertx.eventBus().consumer<String>("amps.messages") { message ->
                    println("Processing message: ${message.body()}")
                    // Add your message processing logic here
                }

                startPromise.complete()
            } catch (e: Exception) {
                println("Failed to start MessageProcessorVerticle: ${e.message}")
                startPromise.fail(e)
            }
        }
    }
}