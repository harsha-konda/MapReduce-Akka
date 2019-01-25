package mapreduce

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import dao.{FlatFile,MapFile, Output}
import common.Uuid

object AppManager {
  def props(): Props = Props(new AppManager)

  final case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}

class AppManager
  extends Actor
  with ActorLogging
  with Uuid {

  override def preStart(): Unit = log.info("AppManager started")

  override def postStop(): Unit = log.info("AppManager stopped")
  var shuffleManger : Option[ActorRef] = None
  var job_output : Option[List[(String,Int)]] = None

  override def receive = {
    case file @FlatFile(requestId, chunk ) =>
      val mapManger = context.actorOf(MapManger.props,s"map-manager-$uuid")
      mapManger ! file
      log.info(s"submitted flat file to map Manger $requestId $chunk")

    case file @ MapFile(_,_) =>
      shuffleManger match {
        case Some(actor) => actor ! file
        case None =>
          shuffleManger = Some(context.actorOf(ShuffleSortManger.props,"shuffle-$uuid"))
      }

    case output @ Output(_,_) => job_output = Some(output.output)

    case "send output" =>  job_output.get.mkString("<br>")
  }

}

