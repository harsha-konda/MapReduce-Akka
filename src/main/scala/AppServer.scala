import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model._
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer

import scala.io.{Source, StdIn}

object AppServer extends  App {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher
  val helloActorRef = system.actorOf(mapreduce.AppManager.props,"hello-actor")

  lazy val homepage =  Source.fromFile("/assets/submit_job.html").toString()
  val route =
    path("submitTask") {
      get {
        helloActorRef ! "say Z"

        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`,homepage))
      }

    } ~
    post {
      path("submitTask") {
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h2>Home</h2>"))
      }
    }


  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
