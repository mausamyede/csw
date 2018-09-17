package csw.config.cli

import java.io.ByteArrayOutputStream
import java.nio.file.Paths
import java.time.Instant

import org.jboss.netty.logging.{InternalLoggerFactory, Slf4JLoggerFactory}
import org.scalatest.{BeforeAndAfterEach, FunSuite, Matchers}

// DEOPSCSW-112: Command line interface client for Configuration service
class ArgsParserTest extends FunSuite with Matchers with BeforeAndAfterEach {

  val relativeRepoPath = "path/in/repository"
  val inputFilePath    = "/tmp/some/input/file"
  val outputFilePath   = "/tmp/some/output/file"
  val id               = "1234"
  val date: String     = Instant.now().toString
  val comment          = "test commit comment!!!"
  val maxFileVersions  = 32

  //Capture output/error generated by the parser, for cleaner test output. If interested, errCapture.toString will return capture errors.
  val outCapture = new ByteArrayOutputStream
  val errCapture = new ByteArrayOutputStream

  // Fix to avoid 'java.util.concurrent.RejectedExecutionException: Worker has already been shutdown'
  InternalLoggerFactory.setDefaultFactory(new Slf4JLoggerFactory)

  override protected def afterEach(): Unit = {
    outCapture.reset()
    errCapture.reset()
  }

  def silentParse(args: Array[String]): Option[Options] =
    Console.withOut(outCapture) {
      Console.withErr(errCapture) {
        new ArgsParser("csw-config-cli").parser.parse(args, Options())
      }
    }

  test("test arguments without specifying operation") {
    val args               = Array("", relativeRepoPath, "-o", outputFilePath)
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test create with no sub-options") {
    val args               = Array("create")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test create with all applicable sub-options") {
    val args               = Array("create", relativeRepoPath, "-i", inputFilePath, "--annex", "-c", comment)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("create",
              Some(Paths.get(relativeRepoPath)),
              Some(Paths.get(inputFilePath)),
              None,
              None,
              None,
              annex = true,
              comment = Some(comment))
    )
  }

  test("test create with bare minimum sub-options") {
    val args               = Array("create", relativeRepoPath, "-i", inputFilePath, "-c", comment)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("create",
              Some(Paths.get(relativeRepoPath)),
              Some(Paths.get(inputFilePath)),
              None,
              None,
              None,
              comment = Some(comment))
    )
  }

  test("test update with no sub-options") {
    val args               = Array("update")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test update with all applicable sub-options") {
    val args               = Array("update", relativeRepoPath, "-i", inputFilePath, "-c", comment)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("update",
              Some(Paths.get(relativeRepoPath)),
              Some(Paths.get(inputFilePath)),
              None,
              None,
              None,
              comment = Some(comment))
    )
  }

  test("test get with no sub-options") {
    val args               = Array("get")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test get with all applicable sub-options") {
    val args               = Array("get", relativeRepoPath, "-o", outputFilePath, "--id", id)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("get", Some(Paths.get(relativeRepoPath)), None, Some(Paths.get(outputFilePath)), Some(id), None)
    )
  }

  test("test get with date sub-option") {
    val args               = Array("get", relativeRepoPath, "-o", outputFilePath, "--date", date)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("get", Some(Paths.get(relativeRepoPath)), None, Some(Paths.get(outputFilePath)), None, Some(Instant.parse(date)))
    )
  }

  test("test get with bare minimum sub-options") {
    val args               = Array("get", relativeRepoPath, "-o", outputFilePath)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("get", Some(Paths.get(relativeRepoPath)), None, Some(Paths.get(outputFilePath)), None, None)
    )
  }

  test("test delete with no sub-options") {
    val args               = Array("delete")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test delete with sub-options") {
    val args               = Array("delete", relativeRepoPath, "-c", comment)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("delete", Some(Paths.get(relativeRepoPath)), None, None, None, None, comment = Some(comment))
    )
  }

  test("test list") {
    val args               = Array("list")
    val x: Option[Options] = silentParse(args)
    x should contain(Options("list", None, None, None, None, None))
  }

  test("test list with pattern") {
    val args               = Array("list", "--pattern", "a/b")
    val x: Option[Options] = silentParse(args)
    x should contain(Options("list", None, None, None, None, None, pattern = Some("a/b")))
  }

  test("test list with type") {
    val args               = Array("list", "--annex")
    val x: Option[Options] = silentParse(args)
    x should contain(Options("list", None, None, None, None, None, annex = true))
  }

  test("test list with type and pattern") {
    val args               = Array("list", "--normal", "--annex", "--pattern", "a/b")
    val x: Option[Options] = silentParse(args)
    x should contain(Options("list", None, None, None, None, None, annex = true, pattern = Some("a/b"), normal = true))
  }

  test("test history with no sub-options") {
    val args               = Array("history")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test history with all sub-options") {
    val args               = Array("history", relativeRepoPath, "--from", date, "--to", date, "--max", maxFileVersions.toString)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("history",
              Some(Paths.get(relativeRepoPath)),
              fromDate = Instant.parse(date),
              toDate = Instant.parse(date),
              maxFileVersions = maxFileVersions)
    )
  }

  test("test history with max sub-option") {
    val args               = Array("history", relativeRepoPath, "--max", maxFileVersions.toString)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("history", Some(Paths.get(relativeRepoPath)), None, None, None, None, maxFileVersions = maxFileVersions)
    )
  }

  test("test history with from and to sub-options") {
    val args               = Array("history", relativeRepoPath, "--from", date, "--to", date)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("history", Some(Paths.get(relativeRepoPath)), fromDate = Instant.parse(date), toDate = Instant.parse(date))
    )
  }

  test("test setActiveVersion with all sub-options") {
    val args               = Array("setActiveVersion", relativeRepoPath, "--id", id, "--comment", comment)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("setActiveVersion", Some(Paths.get(relativeRepoPath)), None, None, Some(id), None, comment = Some(comment))
    )
  }

  test("test resetActiveVersion with no sub-options") {
    val args               = Array("resetActiveVersion")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test resetActiveVersion with sub-options") {
    val args               = Array("resetActiveVersion", relativeRepoPath, "--comment", comment)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("resetActiveVersion", Some(Paths.get(relativeRepoPath)), None, None, None, None, comment = Some(comment))
    )
  }

  test("test getActiveVersion") {
    val args               = Array("getActiveVersion", relativeRepoPath)
    val x: Option[Options] = silentParse(args)
    x should contain(Options("getActiveVersion", Some(Paths.get(relativeRepoPath)), None, None, None, None))
  }

  test("test getActiveByTime with date sub-option") {
    val args               = Array("getActiveByTime", relativeRepoPath, "--date", date, "-o", outputFilePath)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("getActiveByTime",
              Some(Paths.get(relativeRepoPath)),
              None,
              Some(Paths.get(outputFilePath)),
              None,
              Some(Instant.parse(date)))
    )
  }

  test("test historyActive with no sub-options") {
    val args               = Array("historyActive")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test historyActive with all sub-options") {
    val args =
      Array("historyActive", relativeRepoPath, "--from", date, "--to", date, "--max", maxFileVersions.toString)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("historyActive",
              Some(Paths.get(relativeRepoPath)),
              fromDate = Instant.parse(date),
              toDate = Instant.parse(date),
              maxFileVersions = maxFileVersions)
    )
  }

  test("test historyActive with max sub-option") {
    val args               = Array("historyActive", relativeRepoPath, "--max", maxFileVersions.toString)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("historyActive", Some(Paths.get(relativeRepoPath)), None, None, None, None, maxFileVersions = maxFileVersions)
    )
  }

  test("test historyActive with from and to sub-options") {
    val args               = Array("historyActive", relativeRepoPath, "--from", date, "--to", date)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("historyActive", Some(Paths.get(relativeRepoPath)), fromDate = Instant.parse(date), toDate = Instant.parse(date))
    )
  }

  test("test getMetadata") {
    val args               = Array("getMetadata")
    val x: Option[Options] = silentParse(args)
    x should contain(Options("getMetadata", None, None, None, None, None))
  }

  test("test exists with no sub-options") {
    val args               = Array("exists")
    val x: Option[Options] = silentParse(args)
    x shouldEqual None
  }

  test("test exists with sub-options") {
    val args               = Array("exists", relativeRepoPath)
    val x: Option[Options] = silentParse(args)
    x should contain(Options("exists", Some(Paths.get(relativeRepoPath)), None, None, None, None))
  }

  test("test getActive") {
    val args               = Array("getActive", relativeRepoPath, "-o", outputFilePath)
    val x: Option[Options] = silentParse(args)
    x should contain(
      Options("getActive", Some(Paths.get(relativeRepoPath)), None, Some(Paths.get(outputFilePath)), None, None)
    )
  }

}
