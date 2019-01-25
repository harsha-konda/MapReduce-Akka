package mapreduce

import akka.actor.{Actor, ActorRef, Props}
import dao.{MapFile, Output}

import scala.collection.mutable
import common.Uuid
import mapreduce.ShuffleSortManger.{PerformShuffleSort, ShuffleInput, ShuffleOutput}

import scala.concurrent.ExecutionContext
import scala.concurrent.duration._

object ShuffleSortManger {
  def props: Props =
    Props(new ShuffleSortManger)

  final case class ShuffleInput(requestId: String, input1 : List[(String,Int)], input2 : List[(String,Int)] )
  final case class ShuffleOutput(requestId: String, output: List[(String,Int)])
  final case class PerformShuffleSort(requestId: String)

}

class ShuffleSortManger(shuffleSortCount: Int = 2)
  extends Actor
    with Uuid{
  var files = new mutable.Queue[List[(String,Int)]]

  var busyMapPool = mutable.Map.empty[String, ActorRef]
  var freeMapPool = mutable.Map.empty[String, ActorRef]

  def mapFinished = files.size == 1 && busyMapPool.isEmpty

  implicit val ec = ExecutionContext.global

  override def preStart(): Unit = {
    launchShuffleSorts()
    context.system.scheduler.scheduleOnce(2 seconds, self, PerformShuffleSort(uuid))
  }

  override def receive: Receive = {
    case PerformShuffleSort(id) =>
      if (files.size > 1 && freeMapPool.nonEmpty){
        val (id,actor) = freeMapPool.head
        freeMapPool -= id
        freeMapPool += id -> actor
        actor ! ShuffleInput(uuid, files.dequeue(),files.dequeue())
      } else if(mapFinished) {
        context.parent ! ShuffleOutput(uuid,files.head)
      }

    case shuffleFile @ ShuffleOutput(_,_) =>
      files enqueue  shuffleFile.output

    case mapFile @ MapFile(_,_) =>
      files.enqueue(mapFile.output)
      if (files.size > 1) self ! PerformShuffleSort(uuid)

    case output @ Output(_,_) => context.parent ! output

  }


  def launchShuffleSorts() = {
    for (i <- 0 to shuffleSortCount) {
      val id = uuid
      val actorRef = context.actorOf(ShuffleSortActor.props(id),s"shufflesort-${id}")
      freeMapPool +=  id -> actorRef
    }
  }

}
