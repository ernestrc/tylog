package build.unstable.tylog

import build.unstable.tylog.Fixture.MockLogger
import org.scalatest.{Matchers, WordSpec}

class TypedLoggingSpec extends WordSpec with Matchers with TypedLogging {

  override type CallType = String
  override type TraceID = String

  "TypedLogging" should {

    "inject args to template" in {
      {
        val log = new MockLogger(0)

        error(log, new Exception("BOOM"), "A: {}", "a")
        assert(log.interceptedMessage.get == "A: a")

        debug(log, "A: {}", "a")
        assert(log.interceptedMessage.get == "A: a")

        info(log, "A: {}", "a")
        assert(log.interceptedMessage.get == "A: a")

        warning(log, "A: {}", "a")
        assert(log.interceptedMessage.get == "A: a")

        trace(log, "", "", Variation.Attempt, "A: {}", "a")
        assert(log.interceptedMessage.get == "A: a")

      }
    }

    "check missing args and placeholders at compile time" in {

      """
        | val log = new MockLogger(0)
        |error(log, new Exception("BOOM"), "A: {}")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |error(log, new Exception("BOOM"), "A", "a")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |debug(log, "A: {}", "a")""".stripMargin should compile

      """
        | val log = new MockLogger(0)
        |debug(log, "A: {}")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |debug(log, "A", "a")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |info(log, "A: {}")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |info(log, "A", "a")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |warning(log, "A: {}")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |warning(log, "A", "a")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |trace(log, "A", "a", Variation.Attempt, "{}")""".stripMargin shouldNot compile

      """
        | val log = new MockLogger(0)
        |trace(log, "A", "a", Variation.Attempt, "{}", "a")""".stripMargin should compile
    }

    /* TODO inject MDC
    "log trace messages with set MDC context" in {
      val log = new MockLogger(0)
      trace(log, "1", "a", Variation.Attempt, "{}", "a")
      log.interceptedMdc should contain theSameElementsAs Map(
        Macros.callTypeKey → "a",
        Macros.traceIdKey → "1",
        Macros.variationKey → "Attempt"
      )
    }*/

    "not allow non-literal strings if args are passed" in {
      """
        | val log = new MockLogger(0)
        | val msg = "A"
        |info(log, msg, "a")""".stripMargin shouldNot compile
    }

    "allow non-literal strings if no args are passed" in {
       val log = new MockLogger(0)
       val msg = "A"
      info(log, msg)
      assert(log.interceptedMessage.get == "A")
    }

    "not not interpolate string if level is not enabled" in {
      val log = new MockLogger(100)
      info(log, "{}", "a")
      assert(log.interceptedMessage.isEmpty)
    }
  }
}