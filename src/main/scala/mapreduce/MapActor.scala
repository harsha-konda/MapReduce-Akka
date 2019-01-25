package mapreduce

import akka.actor.{Actor, ActorLogging, Props}
import dao.{Data, HealthCheck, MapFile}
import common.Uuid


object MapActor{
  def props(id:String): Props =
    Props(new MapActor(id))
}

class MapActor(id:String)
  extends Actor
    with Uuid
    with ActorLogging{
  type K = List[(String,Int)]


  override def receive: Receive = {
    case Data(_, input) =>
      val data =  Map.map(input)
      log.info(s"sending proceessed data to ${sender()}")
      sender() ! wrapData(data)
    case HealthCheck(requestId) => requestId
  }

  def wrapData(data: K) = {
    MapFile(uuid(),data)
  }
  def healthCheck() = s"i'm healthy $id"
  def getId = id
}


object Map {
//  implicit val f =
//    (x: String) => {
//      x.split("\\s+").map((_, 1)).toList
//    }
//
//  def map[I](input: I)(implicit f: I => List[(I, Int)]): List[(I, Int)] = f(input)

  def map(input: String) = input.split("\\s+").map((_, 1)).toList
}