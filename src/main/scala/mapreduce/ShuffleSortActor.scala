//package mapreduce
//
//import akka.actor.Actor
//import dao.{Data, HealthCheck, ShuffleInput, ShuffleOutput}
//
//class ShuffleSortActor extends Actor{
//
//  override def receive: Receive = {
//    case ShuffleInput(_,file1,file2) =>
//      val sortedOutput = (file1 ++ file2).sortBy(_._1)
//      context.parent ! ShuffleOutput("",sortedOutput)
//      context.parent ! self
//
//    case HealthCheck(requestId) => requestId
//  }
//}
