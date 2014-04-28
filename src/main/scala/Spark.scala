import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

class Spark(sc: SparkContext) extends GitProcessor {

  private val reducer: (AuthorStats, AuthorStats) => AuthorStats = (acc, value) => acc.add(value)

  def apply(url: String): Map[String, AuthorStats] = {
    val broadcastUrl = sc.broadcast(url)
    val tmpRepo = GitRepo(broadcastUrl.value)
    val shas = (tmpRepo.getCommits :+ "").reverse.sliding(2).toSeq
    val commits = sc.parallelize(shas)
    val authStats = commits.map { (parentCommitPair) =>
      require(parentCommitPair.size == 2)
      val repo = GitRepo(broadcastUrl.value)
      val oParent = Option(parentCommitPair(0)).filter(_ ne "").map(repo.getCommit(_))
      val commit = repo.getCommit(parentCommitPair(1))
      val diff = repo.diff(oParent, commit)
      (commit.getAuthorIdent.getEmailAddress, AuthorStats().add(diff))
    }.reduceByKey(reducer)
    authStats.collectAsMap().toMap
  }
}

object Spark extends GitProcessor  {

  val sparkHome = "/Users/tomek/Development/spark-0.9.1" //"/root/spark"
  val sparkUrl = "local"
  val jarFile = "target/scala-2.10/loc-assembly-1.0.jar"

  val sc = new SparkContext(sparkUrl, "loc", sparkHome, Seq(jarFile))
  val spark = new Spark(sc)

  def apply(url: String): Map[String, AuthorStats] = spark(url)
}
