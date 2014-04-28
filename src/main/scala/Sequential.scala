import scala.annotation.tailrec

import org.eclipse.jgit.revwalk.RevWalk

object Sequential extends GitProcessor{

  def apply(url: String): Map[String, AuthorStats] = {
    val repo = GitRepo(url)
    val revWalk = new RevWalk(repo.repo)
    val head = revWalk.parseCommit(repo.repo.getRef("refs/heads/master").getObjectId)
    revWalk.markStart(head)
    val authorStats = Map[String, AuthorStats]().withDefaultValue(AuthorStats(0, 0))
    foldCommits(authorStats, revWalk, repo)
  }

  @tailrec
  private def foldCommits(acc: Map[String, AuthorStats], revWalk: RevWalk, repo: GitRepo): Map[String, AuthorStats] = {
    Option(revWalk.next()) match {
      case Some(revCommit) => {
        val diffs = repo.diff(revCommit)
        val updated = acc.updated(
          revCommit.getAuthorIdent.getEmailAddress,
          diffs.foldLeft(acc(revCommit.getAuthorIdent.getEmailAddress))(
            (stats: AuthorStats, diff: String) => stats.add(diff)))
        foldCommits(updated, revWalk, repo)
      }
      case None => acc
    }
  }
}
