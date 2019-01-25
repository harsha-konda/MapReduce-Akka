import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpEntity, _}
import akka.http.scaladsl.server.Directives._
import akka.stream.ActorMaterializer
import common.Uuid
import dao.FlatFile

import scala.concurrent.duration._
import akka.util.Timeout
import akka.pattern.ask

import scala.io.{Source, StdIn}
import mapreduce.AppManager

import scala.concurrent.Await
object AppServer extends App with Uuid {
  implicit val system = ActorSystem("my-system")
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = system.dispatcher

  val mrManger  = system.actorOf(AppManager.props, "mr-manager")
  var mrJobs = Map.empty[String, Boolean]

  implicit val timeout = Timeout(5 seconds)
  val home = System.getProperty("user.home")
  lazy val homepage =
    Source.fromFile(s"${home}/Stuff/git/MapReduce-Akka/src/assets/submit_job.html").getLines().mkString("\n")

  val route =
    path("submitTask") {
      get {
        mrManger ! "map reduce manager alive"
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, homepage))
      }

    } ~
    post {
      path("submitTask") {
        entity(as[(String)]) { payload =>
          submitMR(payload)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, "<h2>Submitted</h2>"))
        }
      }
    } ~
    get {
      pathPrefix("getTaskStatus" ) {
        path(Segment) { name =>
          val status = getJobProgress(name)
          complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, s"{ $name : $status }"))
        }
      }
    } ~
    get {
      path("getTasksStatus" ) {
        val jobs_status = mrJobs.keys.map((job) => {
          val status = getJobProgress(job)
          s"$job => $status"
        }).mkString("<br/>")
        complete(HttpEntity(ContentTypes.`text/html(UTF-8)`, jobs_status))

      }
    }

  def submitMR(payload: String) = {
    val parameters = payload.split('&').map(_.split('='))
    val job = parameters(0)(1)
    val data = parameters(1)(1)
    mrJobs += job -> false
    mrManger ! FlatFile(uuid(), data)
  }

  def getJobProgress(job:String): String =
    mrJobs.get(job) match {
      case Some(_) =>
//        val f = mrManger ? "send output"
//        Await.result(f,1 second).toString
        "In Progress"

      case None => "Not Found"
    }

  val bindingFuture = Http().bindAndHandle(route, "localhost", 8080)

  println(s"Server online at http://localhost:8080/\nPress RETURN to stop...")
  StdIn.readLine() // let it run until user presses return
  bindingFuture
    .flatMap(_.unbind()) // trigger unbinding from the port
    .onComplete(_ => system.terminate()) // and shutdown when done

}
