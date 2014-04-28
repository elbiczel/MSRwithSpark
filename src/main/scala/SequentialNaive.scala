object SequentialNaive extends GitProcessor {

  def apply(url: String): Map[String, AuthorStats] = {
    val repo = GitRepo(url)
    val authorStats = Map[String, AuthorStats]().withDefaultValue(AuthorStats(0, 0))
    val shas = (repo.getCommits :+ "").reverse.sliding(2).toSeq
    val parentCommitPairs = shas.map { parentCommitPair =>
      require(parentCommitPair.size == 2)
      (Option(parentCommitPair(0)).filter(_ ne "").map(repo.getCommit(_)), repo.getCommit(parentCommitPair(1)))
    }
    val diffs = parentCommitPairs.map { parentWCommit =>
      val oParent = parentWCommit._1
      val commit = parentWCommit._2
      (commit.getAuthorIdent.getEmailAddress, repo.diff(oParent, commit))
    }
    diffs.foldLeft(authorStats) { (stats, authorDiff) =>
      stats.updated(authorDiff._1, stats(authorDiff._1).add(authorDiff._2))
    }
  }
}
