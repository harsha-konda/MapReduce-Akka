package mapreduce

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import dao.{Data, FlatFile, MapFile}

import scala.collection.mutable
import common.Uuid

object MapManger {
  def props: Props =
    Props(new MapManger)
}

class MapManger(numberOfMaps: Int = 3)
  extends Actor
    with Uuid
    with ActorLogging{

  var busyMapPool = mutable.Map.empty[String, ActorRef]
  var freeMapPool = mutable.Map.empty[String, ActorRef]

  override def preStart(): Unit = {
    launchMaps(numberOfMaps)
  }

  override def receive: Receive = {
    case msg@FlatFile(requestId, chunk) =>
      log.info(s"received flat file ${msg.chunk}")
      partition_chunk(msg.chunk).
        foreach(data => {
          val (id,actor) = freeMapPool.head
          freeMapPool -= id
          actor forward data
          busyMapPool += id -> actor
        })

    case msg@MapFile(requestId, chunk) =>
      val actor = sender().asInstanceOf[MapActor]
      val id = actor.getId
      busyMapPool -= id
      freeMapPool += id -> actor.asInstanceOf[ActorRef]
      context.parent ! msg
  }

  def partition_chunk(chunk: String) = {
    val lines = chunk.split("%0D%0").toList
    val partitionSize = Math.max(lines.size / numberOfMaps, 1)
    val slices = lines.grouped(partitionSize)
    slices map {
      case slice => Data(uuid(), slice.mkString(" "))
    }
  }

  def launchMaps(mapsToLaunch: Int) = {
    for (i <- 0 to mapsToLaunch) {
      val id = uuid
      val actorRef = context.actorOf(MapActor.props(id), s"map-$uuid")
      freeMapPool += id -> actorRef
    }
  }

}
