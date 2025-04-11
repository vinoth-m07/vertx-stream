
import com.example.MessageProcessorVerticle
import io.vertx.core.Vertx

fun main() {
    val vertx = Vertx.vertx()
    vertx.deployVerticle(TestPublisher())
    vertx.deployVerticle(MainVerticle())
        .onSuccess {
            // Deploy test publisher in development
            vertx.deployVerticle(TestPublisher())
            /*if (System.getenv("ENVIRONMENT") == "dev") {
                vertx.deployVerticle(TestPublisher())
            }*/
            vertx.deployVerticle(MessageProcessorVerticle())
        }
        .onFailure { e ->
            println("Application failed to start: ${e.message}")
            vertx.close()
        }


}