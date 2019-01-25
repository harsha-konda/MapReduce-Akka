package mapreduce

import akka.actor.{Actor, Props}
import dao.{Data, HealthCheck, MapFile}
import java.util.UUID.randomUUID

object MapActor {
  def props(id:String): Props =
    Props(new MapActor(id))
}

class MapActor(id:String) extends Actor {
  var data: List[(String, Int)] = List()

  override def receive: Receive = {
    case Data(_, input) =>
      data = Map.map(input)
      sender() ! data
    case HealthCheck(requestId) => requestId
  }

  def healthCheck() = s"i'm healthy $id"
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