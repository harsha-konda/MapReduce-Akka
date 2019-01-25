package mapreduce

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}

object AppManager {
  def props(): Props = Props(new AppManager)

  final case class RequestTrackDevice(groupId: String, deviceId: String)
  case object DeviceRegistered
}

class AppManager extends Actor with ActorLogging{

  override def preStart(): Unit = log.info("AppManager started")

  override def postStop(): Unit = log.info("AppManager stopped")


  override def receive = {
    case _ => log.info("hello")


  }

}

