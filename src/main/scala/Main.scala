import akka.actor.typed.ActorSystem
import akka.actor.typed.scaladsl.Behaviors
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.*
import akka.http.scaladsl.model.ws.{TextMessage, Message}
import akka.http.scaladsl.server.Directives.*
import akka.http.scaladsl.server.Route
import akka.stream.scaladsl.{Source, Flow, Sink}
import akka.NotUsed
import akka.http.scaladsl.model.ws.TextMessage.Strict
import akka.http.scaladsl.model.ws.TextMessage.Streamed
import akka.http.scaladsl.model.ws.BinaryMessage
import com.typesafe.config.ConfigFactory

import scala.concurrent.{Future, ExecutionContextExecutor}
import scala.util.Random
import scala.io.StdIn

object WSServer:

  def main(args: Array[String]): Unit =
    val html =
    """
<!DOCTYPE html>
<html>
<head>
  <style>
    html, body {
      background-color:black;
      background-image:
      radial-gradient(white, rgba(255,255,255,.2) 2px, transparent 40px),
      radial-gradient(white, rgba(255,255,255,.15) 1px, transparent 30px),
      radial-gradient(white, rgba(255,255,255,.1) 2px, transparent 40px),
      radial-gradient(rgba(255,255,255,.4), rgba(255,255,255,.1) 2px, transparent 30px);
      background-size: 550px 550px, 350px 350px, 250px 250px, 150px 150px;
      background-position: 0 0, 40px 60px, 130px 270px, 70px 100px;
    }
  </style>
</head>
<body>
</body>
</html>
"""

    val customConf = ConfigFactory.parseString("""
      akka.http.server.remote-address-header = on
    """)

    given system: ActorSystem[Any] = ActorSystem(Behaviors.empty, "system", ConfigFactory.load(customConf))
    given executionContext: ExecutionContextExecutor = system.executionContext

    //val (addr, port) = ("192.168.10.107", 8080)
    val (addr, port) = ("localhost", 8080)

    val greeter: Flow[Message, Message, NotUsed] =
      Flow[Message]
        .mapAsync(1) {
          case text: TextMessage.Strict =>
            println("text (strict) message")
            Future(TextMessage(Source.single("Your message [STRICT] is: " + text)))
          case TextMessage.Streamed(textStream) =>
            textStream.runWith(Sink.ignore)
            sys.error("er")
          case _ => sys.error("")
        }

    val route: Route =
      path("api") {
        extractClientIP { entity =>
          pprint.pprintln(entity)
          handleWebSocketMessages(greeter)
        }
      } ~
      get {
        pathSingleSlash {
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)` , html))
        }
      } ~
      complete(HttpEntity(ContentTypes.`text/html(UTF-8)` , "Page not found"))

    val bindingFuture = Http().newServerAt(addr, port).bind(route)

    println(s"Server now online.")

    StdIn.readLine() // let it run until user presses return
    bindingFuture
      .flatMap(_.unbind()) // trigger unbinding from the port
      .onComplete(_ => system.terminate()) // and shutdown when done
