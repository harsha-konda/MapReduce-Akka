package mapreduce

import akka.actor.{Actor, Props}
import dao.HealthCheck
import mapreduce.ShuffleSortManger.{ShuffleInput, ShuffleOutput}

object ShuffleSortActor{
  def props(id:String): Props =
    Props(new ShuffleSortActor(id))
}
class ShuffleSortActor(id: String) extends Actor{

  override def receive: Receive = {
    case ShuffleInput(_,file1,file2) =>
      val sortedOutput = (file1 ++ file2).sortBy(_._1)
      context.parent ! ShuffleOutput("",sortedOutput)
      context.parent ! self

    case HealthCheck(requestId) => requestId
  }
}
