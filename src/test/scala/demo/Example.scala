package demo

import com.fortysevendeg.lambdatest._
import CaptureLambdaReporter.capture
import akka.actor.ActorSystem
import com.persist.JsonOps._
import com.persist.logging._

// format: OFF

class Example extends LambdaTest {

  val act = label("Initial Tests") {
    test("Eq test") {
      assertEq(2 + 1, 3, "Int eq test")
    }
  } +
  label("Simple Tests") {
    test("Assert Test") {
      assertEq(1, 2, "Bad Int eq test") +
      assert(3 == 5 - 2, "should work")
    }
  }
}

case class Capture() extends ClassLogging {
  val data = capture("example", new Example)
  println(Pretty(data))
  log.alternative("test", data)
}

object Example extends App {
  run("example", new Example)

  val system = ActorSystem("demo")
  val loggingSystem = LoggingSystem(system, "demo", "1.0.0", "localhost")

  Capture()

  loggingSystem.stop
  system.terminate()
}
