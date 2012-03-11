package iterace
import org.scalatest.FunSuite
import org.scalatest.BeforeAndAfter

import iterace.conversions._
import iterace.LoopContextSelector.LoopCallSiteContext
import org.junit.Assert._
import scala.collection._

abstract class RaceTest(dependencies: List[String], startClass: String) extends FunSuite with BeforeAndAfter  {
  def analyze(method: String) = new IteRace(startClass, method, dependencies)

  def printRaces(races: Map[Loop, Map[O, Map[F, RSet]]]): String = {
    val s = new StringBuilder
    s ++= "\n"
    for ((l, lr) <- races) {
      s ++= "Loop: "+l.n.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint() + "\n\n"
      for ((o, fr) <- lr) {
        s ++= o.prettyPrint() + "\n"
        for ((f, rr) <- fr) {
          s ++= " ." + f.getName() + "\n"
          s ++= rr.prettyPrint() +"\n"
        }
      }
    }
    s.toString()
  }
  
  def testResult(method: String, result: String) = {
    test(method) {
      val iterace = analyze(method+"()V")
      assertEquals(result, printRaces(iterace.races))
    }
  }
  
  def testNoRaces(method: String) = testResult(method, "\n")
}