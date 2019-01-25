package dao



final case class Data(requestId: String,input: String)
final case class MapFile(requestId: String,output: List[(String,Int)])


final case class ShuffleInput(requestId: String, input1 : List[(String,Int)], input2 : List[(String,Int)] )
final case class ShuffleOutput(requestId: String, output: List[(String,Int)])

final case class HealthCheck(requestId: String)
final case class FlatFile(requestId: String, chunk: String)