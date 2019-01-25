package mapreduce

import akka.actor.{Actor, ActorLogging, Props}
import dao.{Data, HealthCheck, MapFile}
import common.Uuid
import mapreduce.ReduceActor.ReduceInput


object ReduceActor{
  def props(id:String): Props =
    Props(new ReduceActor(id))

  final case class ReduceInput(id: String,file: List[(String,Int)])
}

class ReduceActor(id:String)
  extends Actor
    with Uuid
    with ActorLogging{

  type K = List[(String,Int)]

  override def receive: Receive = {
    case ReduceInput(_, input) =>
      val data =  Reduce.reduce(input)
      log.info(s"sending proceessed data to ${sender()}")
    case HealthCheck(requestId) => requestId
  }

  def wrapData(data: K) = {
    MapFile(uuid(),data)
  }
  def healthCheck() = s"i'm healthy $id"
  def getId = id
}


object Reduce {
  def reduce(input: List[(String,Int)]) =
    input.groupBy(_._1).map {
      case (key,value) => (key,value.map(_._2).sum)
  }
}