import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

object Spark extends GitProcessor {

  def apply(url: String): Map[String, AuthorStats] = {

    val sparkHome = "/Users/tomek/Development/spark-0.9.1" //"/root/spark"
    val sparkUrl = "local"
    val jarFile = "target/scala-2.10/loc-assembly-1.0.jar"

    val sc = new SparkContext(sparkUrl, "loc", sparkHome, Seq(jarFile))
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

  private val reducer: (AuthorStats, AuthorStats) => AuthorStats = (acc, value) => acc.add(value)
}
