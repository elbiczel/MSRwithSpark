import org.apache.spark.SparkContext
import org.apache.spark.SparkContext._

object Spark extends GitProcessor {

  def apply(url: String): Map[String, AuthorStats] = {

    val sparkHome = "/Users/tomek/Development/spark-0.9.1" //"/root/spark"
    val sparkUrl = "local"
    val jarFile = "target/scala-2.10/loc-assembly-1.0.jar"

    val sc = new SparkContext(sparkUrl, "loc", sparkHome, Seq(jarFile))
    val broadcastUrl = sc.broadcast(url)
    val repo = GitRepo(broadcastUrl.value)
    val commits = sc.parallelize(repo.getCommits)
    val authStats = commits.map { (sha) =>
      val repo = GitRepo(broadcastUrl.value)
      val commit = repo.getCommit(sha)
      val diffs = repo.diff(commit)
      val authorStats = diffs.foldLeft(AuthorStats())(
        (stats: AuthorStats, diff: String) => stats.add(diff))
      (commit.getAuthorIdent.getEmailAddress, authorStats)
    }.reduceByKey(reducer)
    authStats.collectAsMap().toMap
  }

  private val reducer: (AuthorStats, AuthorStats) => AuthorStats =
    (acc, value) => acc.add(value)
}
