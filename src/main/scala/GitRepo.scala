import java.io.{ByteArrayOutputStream, File}

import scala.collection.JavaConversions._

import org.eclipse.jgit.api.Git
import org.eclipse.jgit.diff.{DiffFormatter, RawTextComparator}
import org.eclipse.jgit.lib.{ObjectId, Repository}
import org.eclipse.jgit.revwalk.{RevCommit, RevWalk}
import org.eclipse.jgit.storage.file.FileRepositoryBuilder
import org.eclipse.jgit.treewalk.{CanonicalTreeParser, EmptyTreeIterator}

/**
 * This class is not thread safe!
 */
class GitRepo(val repo: Repository) {

  val reader = repo.newObjectReader()
  val walk = new RevWalk(repo)
  val os = new ByteArrayOutputStream
  val df = new DiffFormatter(os)
  df.setRepository(repo)
  df.setDiffComparator(RawTextComparator.DEFAULT)
  df.setDetectRenames(true)
  val parser: CanonicalTreeParser = new CanonicalTreeParser

  def getCommit(sha: String): RevCommit = {
    walk.parseCommit(ObjectId.fromString(sha))
  }

  def getCommits: Seq[String] = {
    val logs = new Git(repo).log()
        .all()
        .call().toSeq
    logs.map(revCommit => revCommit.getId.name).toSeq
  }

  def diff(parent: Option[RevCommit], revCommit: RevCommit): String = {
    os.reset()
    parent.map { parentCommit =>
      df.scan(parentCommit.getTree, revCommit.getTree)
    }.getOrElse {
      parser.reset(reader, revCommit.getTree)
      df.scan(new EmptyTreeIterator(), parser)
    }.foreach { diffEntry =>
      df.format(diffEntry)
    }
    os.toString
  }
}

object GitRepo {

  def apply(url: String): GitRepo = {
    if (isRemote(url)) {
      println("remote: " + url)
      val localPath = new File("GitRepo")
      if (!localPath.exists()) {
        if (localPath.mkdir()) {
          println("cloning: " + url)
          Git.cloneRepository()
              .setURI(url)
              .setDirectory(localPath)
              .call()
          println("cloned: " + url)
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
