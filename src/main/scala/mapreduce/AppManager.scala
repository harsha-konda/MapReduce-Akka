package mapreduce

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
import dao.FlatFile
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


  override def receive = {
    case file @FlatFile(requestId, chunk ) =>
      val mapManger = context.actorOf(MapManger.props,s"map-manager-$uuid")
      mapManger ! file
      log.info(s"submitted flat file to map Manger $requestId $chunk")

    case _ => log.info("hello")


  }

}

