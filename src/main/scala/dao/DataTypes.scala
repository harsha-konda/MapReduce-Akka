package dao


final case class Data(requestId: String,input: String)
final case class MapFile(requestId: String,output: List[(String,Int)])

final case class HealthCheck(requestId: String)

final case class MRJob(requestId: String,jobname: String, chunk: String)
final case class FlatFile(requestId: String, chunk: String)

final case class Output(requestId: String , output: List[(String,Int)])