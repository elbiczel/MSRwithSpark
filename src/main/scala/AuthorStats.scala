case class AuthorStats(deletions: Int, additions: Int) {

  def add(diffEntry: String): AuthorStats = {
    val lines = diffEntry.split("\n")
    val newDeletions = lines.filter(line => line.startsWith("-")).size
    val newAdditions = lines.filter(line => line.startsWith("+")).size
    AuthorStats(newDeletions + deletions, newAdditions + additions)
  }

  def add(other: AuthorStats): AuthorStats = {
    AuthorStats(deletions + other.deletions, additions + other.additions)
  }
}

object AuthorStats {

  def apply(): AuthorStats = AuthorStats(0, 0)
}
