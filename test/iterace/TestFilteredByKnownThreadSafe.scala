package iterace
import org.junit.runner.RunWith
import org.scalatest.junit.JUnitRunner

@RunWith(classOf[JUnitRunner])
class TestFilteredByKnownThreadSafe extends RaceTest(List("particles", "../lib/parallelArray.mock"), "Lparticles/Particle") {
  override def result(iteRace: IteRace) = iteRace.filteredPossibleRaces
  
  testNoRaces("noRaceOnStringConcatenation")
  testResult("noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape","""
Loop: particles.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:461)

particles.Particle.noRaceOnObjectsFromTheCurrentIterationThatHaveOrWillEscape(Particle.java:459)
 .origin
   (a)  particles.Particle$33.op(Particle$33.java:465)
   (b)  particles.Particle$33.op(Particle$33.java:465)
""")
  testNoRaces("noRaceWhenPrintln");
}