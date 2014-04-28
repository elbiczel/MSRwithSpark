import java.io.{ByteArrayOutputStream, File}

import org.eclipse.jgit.treewalk.{EmptyTreeIterator, CanonicalTreeParser}
import scala.collection.JavaConversions._

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.{DiffFormatter, RawTextComparator}
import org.eclipse.jgit.lib.Repository
import org.eclipse.jgit.revwalk.{RevWalk, RevCommit}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder

class GitRepo(val repo: Repository) {

  val reader = repo.newObjectReader()

  def getCommit(sha: String): RevCommit = {
    val walk = new RevWalk(repo)
    walk.parseCommit(repo.getRef(sha).getObjectId)
  }

  def getCommits: Seq[String] = {
    val logs = new Git(repo).log()
        .all()
        .call().toSeq
    logs.map(revCommit => revCommit.getId.name).toSeq
  }

  def diff(revCommit: RevCommit): Seq[String] = {
    val os = new ByteArrayOutputStream
    val df = new DiffFormatter(os)
    df.setRepository(repo)
    df.setDiffComparator(RawTextComparator.DEFAULT)
    df.setDetectRenames(true)
    val diffs = if (revCommit.getParentCount > 0) {
      val parentCommit = revCommit.getParent(0)
      df.scan(parentCommit.getTree, revCommit.getTree).toSeq
    } else {
      val parser: CanonicalTreeParser = new CanonicalTreeParser
      parser.reset(reader, revCommit.getTree)
      df.scan(new EmptyTreeIterator(), parser).toSeq
    }
    diffs.map { diffEntry =>
      os.reset()
      df.format(diffEntry)
      os.toString
    }
  }
}

object GitRepo {

  def apply(url: String): GitRepo = {
    if (isRemote(url)) {
      val localPath = new File("GitRepo")
      if (!localPath.exists()) {
        if (localPath.mkdir()) {
          Git.cloneRepository()
              .setURI(url)
              .setDirectory(localPath)
              .call()
        }
      }
      getLocalRepo(new File(localPath, ".git"))
    } else {
      getLocalRepo(if (url.endsWith(".git")) new File(url) else new File(url, ".git"))
    }
  }

  def getLocalRepo(gitFile: File): GitRepo = {
    new GitRepo(new FileRepositoryBuilder()
        .setGitDir(gitFile)
        .readEnvironment() // scan environment GIT_* variables
        .findGitDir() // scan up the file system tree
        .build())
  }

  private def isRemote(path: String): Boolean = path.startsWith("git@") || path.startsWith("http")
}
