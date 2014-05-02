import org.apache.spark.{SparkConf, SparkContext}
import org.apache.spark.SparkContext._

class SparkImpl(sc: SparkContext, parralelismCreator: (Int => Int)) extends GitProcessor {

  private val reducer: (AuthorStats, AuthorStats) => AuthorStats = (acc, value) => acc.add(value)

  def apply(url: String): Map[String, AuthorStats] = {
    val broadcastUrl = sc.broadcast(url)
    val tmpRepo = GitRepo(broadcastUrl.value)
    val shas = (tmpRepo.getCommits :+ "").reverse.sliding(2).toSeq
    val commits = sc.parallelize(shas, parralelismCreator(shas.size))
    val authStats = commits.map { (parentCommitPair) =>
      require(parentCommitPair.size == 2)
      val repo = GitRepo(broadcastUrl.value)
      val oParent = if (parentCommitPair(0) == "") { None } else { Some(repo.getCommit(parentCommitPair(0))) }
      val commit = repo.getCommit(parentCommitPair(1))
      val diff = repo.diff(oParent, commit)
      (commit.getAuthorIdent.getEmailAddress, AuthorStats().add(diff))
    }.reduceByKey(reducer)
    authStats.collectAsMap().toMap
  }
}

private[this] case class SparkWithContext(
    sparkHome: String = "/root/spark",
    jarFile: String = "/root/jars/loc.jar",
    sparkUrl: String = "spark://localhost:7077",
    sparkMaxCores: Option[String] = None) extends GitProcessor {

  def apply(url: String): Map[String, AuthorStats] = {
    val conf = new SparkConf()
        .setSparkHome(sparkHome)
        .setMaster(sparkUrl)
        .setAppName("MSR-Loc")
        .setJars(Seq(jarFile))
        .set("spark.executor.memory", "12g")
    if (sparkMaxCores.isDefined) {
      conf.set("spark.cores.max", sparkMaxCores.get)
    }
    val sc = new SparkContext(conf)
    val spark = new SparkImpl(sc, (commitsCount) => commitsCount / 2)
    spark(url)
  }
}
