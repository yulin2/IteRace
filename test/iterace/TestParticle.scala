package iterace;

import org.junit.runner.RunWith
import org.scalatest.{ Spec, BeforeAndAfter }
import org.scalatest.junit.JUnitRunner
import scala.collection.JavaConversions._
import iterace.WALAConversions._
import org.junit.Assert._
import iterace.LoopContextSelector.LoopCallSiteContext
import scala.collection.mutable._
import org.scalatest.FunSuite
import org.junit.rules.TestName
import org.junit.Rule

@RunWith(classOf[JUnitRunner])
class TestParticle extends FunSuite with BeforeAndAfter {
  
  @Rule val testName = new TestName();

  val dependencies = List("particles", "../lib/parallelArray.mock")
  val startClass = "Lparticles/Particle"

  def analyze(method: String) = new IteRace(startClass, method, dependencies)

  def printRaces(races: Map[N, Map[O, Map[F, RSet]]]): String = {
    val s = new StringBuilder
    s ++= "\n"
    for ((l, lr) <- races) {
      s ++= "Loop: "+l.getContext().asInstanceOf[LoopCallSiteContext].prettyPrint() + "\n\n"
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
  
  testNoRaces("vacuouslyNoRace")
  
  testNoRaces("noRaceOnParameter")
  
  testNoRaces("noRaceOnParameterInitializedBefore")
  
  testResult("verySimpleRace", 
      """
Loop: particles.Particle.verySimpleRace(Particle.java:68)

particles.Particle.verySimpleRace(Particle.java:66)
 .x
   (a)  particles.Particle$5.op(Particle$5.java:71)
   (b)  particles.Particle$5.op(Particle$5.java:71)
""")  

	testResult("raceOnParameterInitializedBefore", 
	    """
Loop: particles.Particle.raceOnParameterInitializedBefore(Particle.java:92)

particles.Particle.raceOnParameterInitializedBefore(Particle.java:81)
 .x
   (a)  particles.Particle$7.op(Particle$7.java:95)
   (b)  particles.Particle$7.op(Particle$7.java:95)
""")

	testNoRaces("noRaceOnANonSharedField")

	// but we actually solve it from the loop context
	testNoRaces("oneCFANeededForNoRaces")

	// but we actually solve it from the loop context
	testNoRaces("twoCFANeededForNoRaces")

	testNoRaces("recursive")
	
	testResult("disambiguateFalseRace", """
Loop: particles.Particle.disambiguateFalseRace(Particle.java:189)

particles.Particle.disambiguateFalseRace(Particle.java:186)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
""")

	testNoRaces("ignoreFalseRacesInSeqOp")
	
	testResult("raceBecauseOfOutsideInterference","""
Loop: particles.Particle.raceBecauseOfOutsideInterference(Particle.java:232)

particles.Particle.raceBecauseOfOutsideInterference(Particle.java:229)
 .origin
   (a)  particles.Particle$15.op(Particle$15.java:235) [2]
   (b)  particles.Particle$15.op(Particle$15.java:235)
        particles.Particle$15.op(Particle$15.java:236)
particles.Particle$15.op(Particle$15.java:235)
 .x
   (a)  particles.Particle$15.op(Particle$15.java:236)
   (b)  particles.Particle$15.op(Particle$15.java:236)
""")

	testResult("raceOnSharedObjectCarriedByArray","""
Loop: particles.Particle.raceOnSharedObjectCarriedByArray(Particle.java:259)

particles.Particle$16.op(Particle$16.java:253)
 .y
   (a)  particles.Particle.moveTo(Particle.java:17)
   (b)  particles.Particle.moveTo(Particle.java:17)
 .x
   (a)  particles.Particle.moveTo(Particle.java:16)
   (b)  particles.Particle.moveTo(Particle.java:16)
""")

testResult("raceBecauseOfDirectArrayLoad","""
Loop: particles.Particle.raceBecauseOfDirectArrayLoad(Particle.java:274)

particles.Particle$18.op(Particle$18.java:279)
 .x
   (a)  particles.Particle$18.op(Particle$18.java:278)
   (b)  particles.Particle$18.op(Particle$18.java:278)
particles.Particle.raceBecauseOfDirectArrayLoad(Particle.java:271)
 .x
   (a)  particles.Particle$18.op(Particle$18.java:278)
   (b)  particles.Particle$18.op(Particle$18.java:278)
""")

testResult("raceOnSharedReturnValue", """
Loop: particles.Particle.raceOnSharedReturnValue(Particle.java:290)

particles.Particle.raceOnSharedReturnValue(Particle.java:288)
 .x
   (a)  particles.Particle$19.op(Particle$19.java:293)
   (b)  particles.Particle$19.op(Particle$19.java:293)
""")

testResult("raceOnDifferntArrayIteration", """
Loop: particles.Particle.raceOnDifferntArrayIteration(Particle.java:317)

particles.Particle$20.op(Particle$20.java:306)
 .x
   (a)  particles.Particle$22.op(Particle$22.java:320)
   (b)  particles.Particle$22.op(Particle$22.java:320)
""")

ignore("noRaceIfFlowSensitive") { } // should return no races

testResult("raceOnDifferntArrayIterationOneLoop","""
Loop: particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:367)

particles.Particle.raceOnDifferntArrayIterationOneLoop(Particle.java:365)
 .origin
   (a)  particles.Particle$27.op(Particle$27.java:371) [2]
   (b)  particles.Particle$27.op(Particle$27.java:371)
        particles.Particle$27.op(Particle$27.java:372)
particles.Particle$27.op(Particle$27.java:371)
 .x
   (a)  particles.Particle$27.op(Particle$27.java:370)
   (b)  particles.Particle$27.op(Particle$27.java:370)
""")

// testResult("verySimpleRaceWithIndex","")

// verySimpleRaceToStatic

// raceOnSharedFromStatic

// raceInLibrary
}