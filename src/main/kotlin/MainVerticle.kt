
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import io.vertx.kotlin.coroutines.await
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import com.crankuptheamps.client.Client
import com.crankuptheamps.client.Message
import com.crankuptheamps.client.MessageStream
import com.crankuptheamps.client.exception.AMPSException
import java.util.concurrent.TimeUnit

class MainVerticle : AbstractVerticle() {
    private val ampsServerUrl = "tcp://localhost:9007"
    private val ampsTopic = "sample-topic"
    private val reconnectDelay = 5000L
    private val messageTimeout = 10000L // 10 seconds

    // AMPS client should be initialized when needed, not at class level
    private var ampsClient: Client? = null
//    val config = vertx.orCreateContext.config()
  //  val ampsServerUrl1 = config.getString("amps.serverUrl", "tcp://localhost:9007")

    override fun start(startPromise: Promise<Void>) {
        CoroutineScope(vertx.dispatcher()).launch {
            try {
                initializeAmpsConnection(startPromise)
            } catch (e: Exception) {
                println("Initialization failed: ${e.message}")
                attemptReconnect(startPromise)
            }
        }

        vertx.createHttpServer()
            .requestHandler { req ->
                when (req.path()) {
                    "/health" -> {
                        //val status = if (ampsClient?.isConnected() == true) 200 else 503
                        req.response().setStatusCode(200).end()
                    }
                    "/metrics" -> {
                        // Add custom metrics here
                        req.response().end("OK")
                    }
                }
            }
            .listen(8080)
    }

    private suspend fun initializeAmpsConnection(startPromise: Promise<Void>) {
        try {
            // Create new client for each connection attempt
            val client = Client("vertx-amps-client-${System.currentTimeMillis()}")
                .apply {
                    connect(ampsServerUrl)
                    ampsClient = this
                }

            println("Successfully connected to AMPS server")

            // Set up subscription with bookmark
            val stream = client.subscribe(ampsTopic)
            println("Successfully subscribed to topic: $ampsTopic")

            startPromise.complete()
            processMessages(client, stream)
        } catch (e: Exception) {
            ampsClient?.close()
            throw e
        }
    }

    private suspend fun processMessages(client: Client, stream: MessageStream) {
        while (true) {
            try {
                val message = try {
                    stream.timeout(messageTimeout.toInt()).next() // Use timeout
                } catch (e: AMPSException) {
                    println("AMPS error receiving message: ${e.message}")
                    null
                }

                message?.let {
                    vertx.eventBus().publish("amps.messages", it.data)
                    println("Processed message: ${it.data.take(100)}...") // Log first 100 chars
                } ?: run {
                    println("No message received (timeout or empty stream)")
                }
            } catch (e: Exception) {
                println("Message processing error: ${e.message}")
                attemptReconnect(Promise.promise())
                break
            }
        }
    }

    private fun attemptReconnect(promise: Promise<Void>) {
        ampsClient?.close()
        println("Attempting reconnect in $reconnectDelay ms...")
        vertx.setTimer(reconnectDelay) {
            CoroutineScope(vertx.dispatcher()).launch {
                try {
                    initializeAmpsConnection(promise)
                } catch (e: Exception) {
                    println("Reconnect failed: ${e.message}")
                    attemptReconnect(promise)
                }
            }
        }
    }

    override fun stop() {
        ampsClient?.close()
        println("AMPS client connection closed")
    }
}