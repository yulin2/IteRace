package iterace.stage

import org.junit.Test
import iterace.IteRaceOption
import sppa.util.debug

class TestKnownThreadSafeWild extends RaceAbstractTest {

  val entryClass = "Lparticles/ParticleUsingLibrary"

  override val options = Set[IteRaceOption](IteRaceOption.TwoThreads, IteRaceOption.Filtering)

  debug.activate

  @Test def noRaceWhenPrintln = expectNoRaces
  @Test def noRaceOnPattern = expectNoRaces
  @Test def noRaceOnSafeMatcher = expectNoRaces
  @Test def raceOnUnsafeMatcher = expectSomeRaces
  @Test def noRaceOnSynchronizedList = expectNoRaces

  //  @Test def racePastKnownThreadSafe = expect("""
  //Loop: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:35)
  //
  //particles.Particle: particles.ParticleWithKnownThreadSafe.racePastKnownThreadSafe(ParticleWithKnownThreadSafe.java:33)
  // .x
  //   (a)  particles.Particle.moveTo(Particle.java:16)
  //   (b)  particles.Particle.moveTo(Particle.java:16)
  // .y
  //   (a)  particles.Particle.moveTo(Particle.java:17)
  //   (b)  particles.Particle.moveTo(Particle.java:17)
  //""")

}