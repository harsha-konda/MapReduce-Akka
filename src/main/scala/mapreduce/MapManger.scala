package mapreduce

import akka.actor.{Actor, ActorRef, Props}
import dao.{Data, MapFile,FlatFile}

import scala.collection.mutable
import java.util.UUID.randomUUID


object MapManger {
  def props: Props =
    Props(new MapManger)
}

class MapManger(numberOfMaps: Int = 3) extends Actor{
  var busyMapPool = new mutable.Queue[ActorRef]
  var freeMapPool = new mutable.Queue[ActorRef]

  override def preStart(): Unit = {
    launchMaps(numberOfMaps)
  }

  override def receive: Receive = {
    case msg @FlatFile(requestId, chunk) =>
      partition_chunk(msg.chunk).
        foreach(data => {
          val mapActor = freeMapPool.dequeue()
          mapActor forward data
        })

    case msg @ MapFile(requestId,chunk) =>
      context.parent ! msg
  }

  def partition_chunk(chunk:String) = {
    val lines = chunk.split("\n").toList
    val partitionSize = lines.size / numberOfMaps

    val chunks = for (i <- 0 to partitionSize) yield {
      lines.slice(i*partitionSize,(i+1)*partitionSize)
    }
    chunks map {
      case chunk => Data(randomUUID().toString,chunk.mkString(" "))
    }
  }

  def launchMaps(mapsToLaunch : Int) = {
    for (i <- 0 to mapsToLaunch) {
      val uuid = randomUUID().toString
      val actorRef = context.actorOf(MapActor.props(uuid),s"map-${uuid}")
      freeMapPool +=  actorRef
    }
  }

}
