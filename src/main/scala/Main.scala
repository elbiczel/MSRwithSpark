object Main {

  def main (args: Array[String]) {
    val processor: GitProcessor = if (args.contains("-spark")) {
      Spark
    } else if (args.contains("-naive")) {
      SequentialNaive
    } else {
      Sequential
    }
    val commitStats = processor(args.lastOption.getOrElse("/Users/tomek/srcs/volley"))
    commitStats.foreach((stats) => println("%s stats: %s".format(stats._1, stats._2)))
  }
}
