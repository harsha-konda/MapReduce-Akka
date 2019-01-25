//package mapreduce
//
//import akka.actor.{Actor, ActorRef, Props}
//import dao.{Data, FlatFile, MapFile, ShuffleInput,ShuffleOutput}
//
//import scala.collection.mutable
//import java.util.UUID.randomUUID
//
//import scala.concurrent.duration._
//import scala.collection.mutable.ArrayBuffer
//
//object MapManger {
//  def props: Props =
//    Props(new MapManger)
//}
//
//class ShuffleSortManger(shuffleSortCount: Int = 2) extends Actor{
//  var files = new mutable.Queue[List[(String,Int)]]
//  var busyShufflePool = new mutable.Queue[ActorRef]
//  var freeShufflePool = new mutable.Queue[ActorRef]
//  var mapFinished = false
//
//  override def preStart(): Unit = {
//    launchShuffleSorts()
//    context.system.scheduler.scheduleOnce(30 seconds,self,"shuffle sort")
//  }
//
//  override def receive: Receive = {
//    case "shuffle sort" =>
//      if (files.size > 1 && freeShufflePool.nonEmpty){
//        val shuffle = freeShufflePool.dequeue()
//        busyShufflePool.enqueue(shuffle)
//        shuffle ! ShuffleInput(uuid, files.dequeue(),files.dequeue())
//      } else if(mapFinished && files.size == 1) {
//        context.parent ! files.head
//      }
//
//    case shuffleFile @ ShuffleOutput(_,_) =>
//      files enqueue  shuffleFile.output
//
//    case _ : ActorRef =>
//      freeShufflePool.enqueue(_)
//
//    case mapFile @ MapFile(_,_) =>
//      files.enqueue(mapFile.output)
//      if (files.size > 1) self !  "shuffle sort"
//
//    case "map finished" =>   mapFinished = true
//
//  }
//
//  def uuid :String = randomUUID().toString
//
//  def launchShuffleSorts() = {
//    for (i <- 0 to shuffleSortCount) {
//      val actorRef = context.actorOf(MapActor.props(uuid),s"map-${uuid}")
//      freeMapPool +=  actorRef
//    }
//  }
//
//}
