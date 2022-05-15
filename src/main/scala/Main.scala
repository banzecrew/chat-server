import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.ws.{ TextMessage, Message }
import akka.http.scaladsl.server.Directives.*
import akka.stream.scaladsl.{ Source, Flow }
import scala.io.StdIn

object HttpServerRoutingMinimal:

  def main(args: Array[String]): Unit =

    implicit val system = ActorSystem(Behaviors.empty, "my-system")
    implicit val executionContext = system.executionContext

    val (addr, port) = ("localhost", 8080)

    val greeter = 
      Flow[Message]
        .collect {
          case tm: TextMessage => TextMessage(Source.single("Hello ") ++ tm.textStream)
        }
      
    val route =
      path("hello") {
        get {
          handleWebSocketMessages(greeter)
        }
      }

    val bindingFuture = Http().newServerAt(addr, port).bind(route)

    println(s"Server now online.")
    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
