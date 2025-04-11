
import io.vertx.core.AbstractVerticle
import io.vertx.core.Promise
import com.crankuptheamps.client.Client
import com.crankuptheamps.client.Message
import java.nio.charset.Charset
import java.nio.charset.CharsetDecoder
import java.nio.charset.CharsetEncoder
import java.nio.charset.StandardCharsets

class TestPublisher : AbstractVerticle() {
    private val ampsServerUrl = "tcp://localhost:9007"
    private val ampsTopic = "sample-topic"
    private val publishInterval = 3000L // 3 seconds
    private var ampsClient: Client? = null
    private var timerId: Long? = null

    // Create encoder/decoder pair for UTF-8
    private val utf8Encoder: CharsetEncoder = StandardCharsets.UTF_8.newEncoder()
    private val utf8Decoder: CharsetDecoder = StandardCharsets.UTF_8.newDecoder()

    override fun start(startPromise: Promise<Void>) {
        try {
            // Initialize AMPS client
            ampsClient = Client("test-publisher").apply {
                connect(ampsServerUrl)
                println("Connected to AMPS server at $ampsServerUrl")
            }

            // Start periodic publishing
            timerId = vertx.setPeriodic(publishInterval) { _ ->
                try {
                    // Create message with encoder/decoder
                  /*  val msg = Message(utf8Encoder, utf8Decoder).apply {
                        topic = ampsTopic
                        data = mapOf(
                            "timestamp" to System.currentTimeMillis(),
                            "message" to "Hello from Vert.x publisher",
                            "source" to "test-publisher"
                        ).toString()
                    }*/
                    val data = mapOf(
                        "timestamp" to System.currentTimeMillis(),
                        "message" to "Hello from Vert.x publisher",
                        "source" to "test-publisher"
                    ).toString()

                    ampsClient?.publish(ampsTopic, data)
                    println("Published message to $ampsTopic")
                } catch (e: Exception) {
                    println("Failed to publish message: ${e.message}")
                    e.printStackTrace()
                }
            }

            startPromise.complete()
        } catch (e: Exception) {
            startPromise.fail(e)
            e.printStackTrace()
        }
    }

    override fun stop() {
        timerId?.let { vertx.cancelTimer(it) }
        ampsClient?.disconnect()
        println("TestPublisher stopped")
    }
}