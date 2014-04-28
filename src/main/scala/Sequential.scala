import scala.annotation.tailrec

import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}

object Sequential extends GitProcessor {

  def apply(url: String): Map[String, AuthorStats] = {
    val repo = GitRepo(url)
    val revWalk = new RevWalk(repo.repo)
    val head = revWalk.parseCommit(repo.repo.getRef("refs/heads/master").getObjectId)
    revWalk.markStart(head)
    val authorStats = Map[String, AuthorStats]().withDefaultValue(AuthorStats(0, 0))
    foldCommits(authorStats, revWalk, None, repo)
  }

  @tailrec
  private def foldCommits(
      acc: Map[String, AuthorStats],
      revWalk: RevWalk,
      current: Option[RevCommit],
      repo: GitRepo): Map[String, AuthorStats] = {
    (Option(revWalk.next()), current) match {
      case (Some(parent), None) => foldCommits(acc, revWalk, Some(parent), repo)
      case (Some(parentCommit), Some(revCommit)) => {
        val diff = repo.diff(Some(parentCommit), revCommit)
        val updated = acc.updated(
          revCommit.getAuthorIdent.getEmailAddress,
          acc(revCommit.getAuthorIdent.getEmailAddress).add(diff))
        foldCommits(updated, revWalk, Some(parentCommit), repo)
      }
      case (None, Some(revCommit)) => {
        val diff = repo.diff(None, revCommit)
        val updated = acc.updated(
          revCommit.getAuthorIdent.getEmailAddress,
          acc(revCommit.getAuthorIdent.getEmailAddress).add(diff))
        updated
      }
      case (None, None) => acc
    }
  }
}
