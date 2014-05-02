case class AuthorStats(deletions: Long, additions: Long) {

  def add(diffEntry: String): AuthorStats = {
    val lines = diffEntry.split("\n")
    val newDeletions = lines.filter(line => line.startsWith("-")).size
    val newAdditions = lines.filter(line => line.startsWith("+")).size
    AuthorStats(newDeletions + deletions, newAdditions + additions)
  }

  def addAll(diffs: Seq[String]): AuthorStats = {
    val diffLines = diffs.flatMap(_.split("\n"))
    val newAdditions = diffLines
        .filter(line => line.startsWith("+"))
        .size
    val newDeletions = diffLines
        .filter(line => line.startsWith("-"))
        .size
    AuthorStats(newDeletions + deletions, newAdditions + additions)
  }

  def add(other: AuthorStats): AuthorStats = {
    AuthorStats(deletions + other.deletions, additions + other.additions)
  }
}

object AuthorStats {

  def apply(): AuthorStats = AuthorStats(0, 0)
}
