package com.fortysevendeg.lambdatest

import LambdaOptions._

import scala.annotation.tailrec

object CaptureLambdaReporter {

  def capture(name: String, tests: LambdaTest): Map[String, Any] = {
    val r = CaptureLambdaReporter(name)
    val t0 = LambdaState(reporter = r)
    val a = tests.act.eval(t0)
    val data = a.reporter.asInstanceOf[CaptureLambdaReporter].getData()
    data
  }

  def apply(name: String) = new CaptureLambdaReporter(name)
}

case class CaptureLambdaReporter private[lambdatest] (
  name: String,
  tests: Int = 0,
  failed: Int = 0,
  options: LambdaOptions = InitialLambdaOptions,
  data: List[Map[String, Any]] = List.empty[Map[String, Any]]
) extends LambdaReporter {

  private def getInt(s: String): Any = {
    try {
      s.toInt
    } catch {
      case ex: Throwable ⇒ ""
    }
  }

  private def getParts(s: String): Map[String, Any] = {
    val i = s.indexOf("]")
    val (data, s1) = if (s.startsWith("[") && i >= 0) {
      (s.substring(1, i), s.substring(i + 1).trim)
    } else {
      ("", s)
    }
    val j = s1.lastIndexOf("(")
    val (msg, file, line) = if (s1.endsWith(")") && j >= 0) {
      val pos = s1.substring(j + 1, s1.size - 1)
      val parts = pos.split(" ")
      val file = parts(0)
      val line = if (parts.size == 3) getInt(parts(2)) else ""
      (s1.substring(0, j - 1), file, line)
    } else {
      (s1, "", "")
    }
    val posMap = if (file != "") Map("pos" → Map("file" → file, "line" → line)) else Map()
    val valueMap = if (data == "") Map() else Map("data" → data)
    Map("msg" → msg) ++ valueMap ++ posMap
  }

  def ok(name: String): LambdaReporter = {
    this.copy(tests = tests + 1)
  }

  def fail(name: String): LambdaReporter = {
    this.copy(tests = tests + 1, failed = failed + 1)
  }

  def report(depth: Int, s: String): LambdaReporter = {
    val d = if (s.startsWith("Test: ")) {
      val msg = s.replace("Test: ", "")
      Map("cmd" → "test", "msg" → msg, "depth" → depth)

    } else {
      Map("cmd" → "label", "msg" → s, "depth" → depth)
    }
    this.copy(data = d +: data)
  }

  def reportOk(depth: Int, s: String): LambdaReporter = {
    val msg = s.replace("Ok: ", "")
    val d = Map("cmd" → "assert", "ok" → true, "depth" → depth) ++ getParts(msg)
    this.copy(data = d +: data)
  }

  def reportFail(depth: Int, s: String): LambdaReporter = {
    val msg = s.replace("Fail: ", "")
    val d = Map("cmd" → "assert", "ok" → false, "depth" → depth) ++ getParts(msg)
    this.copy(data = d +: data)
  }

  def changeOptions(change: (LambdaOptions) ⇒ LambdaOptions): LambdaReporter = {
    this.copy(options = change(this.options))
  }

  def getData(): Map[String, Any] = {

    def isOk(d: Any): Boolean = {
      d match {
        case s: List[_] ⇒
          s.forall(x ⇒ isOk(x))
        case m: Map[String @unchecked, _] ⇒
          if (m.contains("cmd") && m("cmd") == "assert") {
            m.get("ok") match {
              case Some(b: Boolean) ⇒ b
              case _ ⇒ true
            }
          } else {
            m.get("sub") match {
              case Some(sub) ⇒ isOk(sub)
              case _ ⇒ true
            }
          }
        case _ ⇒ true
      }
    }

    @tailrec def reduce(size: Int, t: List[List[Map[String, Any]]]): List[List[Map[String, Any]]] = {
      if (t.size > size) {
        val l1 +: l2 +: tail = t
        val h1 +: tail1 = l2
        val MapOk = if (h1.contains("cmd") && h1("cmd") == "test") {
          Map("ok" → isOk(l1))
        } else {
          Map()
        }
        val t1 = ((h1 ++ MapOk ++ Map("sub" → l1.reverse)) +: tail1) +: tail
        reduce(size, t1)
      } else {
        t
      }
    }

    def getDepth(m: Map[String, Any]): Int = {
      m.get("depth") match {
        case Some(i: Int) ⇒ i
        case _ ⇒ 0
      }
    }

    val d = data.reverse
    val t2 = d.foldLeft(List.empty[List[Map[String, Any]]]) {
      case (t, i) ⇒
        val depth = getDepth(i) + 1
        val ct = i - "depth"
        if (depth == t.size) {
          val head +: tail = t
          (ct +: head) +: tail
        } else if (depth > t.size) {
          List(ct) +: t
        } else {
          val t1 = reduce(depth, t)
          val head +: tail = t1
          (ct +: head) +: tail
        }
    }
    val sub = reduce(1, t2).head.reverse
    Map("name" → name, "result" → Map("tests" → tests, "failed" → failed), "sub" → sub)
  }

}
