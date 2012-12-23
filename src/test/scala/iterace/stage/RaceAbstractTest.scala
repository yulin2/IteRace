package iterace.stage

import org.junit.Test
import iterace.datastructure.ProgramRaceSet
import org.junit.Assert._
import iterace.IteRace
import iterace.IteRaceOption
import sppa.util.debug
import wala.Dependency
import wala.WALAConversions._
import wala.S
import iterace.datastructure.LockSets
import wala.AnalysisOptions
import sppa.util.JavaTest
import com.typesafe.config.ConfigFactory
import com.typesafe.config.ConfigParseOptions
import com.typesafe.config.ConfigResolveOptions

abstract class RaceAbstractTest extends JavaTest {
  debug.activate

  def entryClass: String

  def localConfig = "wala.entry { " +
    "class: " + entryClass + "\n" +
    "method: " + testName.getMethodName() + "()V \n" +
    "}\n"

  def config = ConfigFactory.parseString(localConfig) withFallback ConfigFactory.load("test")

  def analysis: IteRace = IteRace(AnalysisOptions()(config), options)
  def options: Set[IteRaceOption]

  def printRaces(iterace: IteRace): String = "\n" + iterace.races.prettyPrint(
    { s: S[I] =>
      iterace.lockSetMapping.getLoopFor(s.n) match {
        case Some(_) => iterace.lockSetMapping.getLockSet(s).map("         " + _.prettyPrint).reduceOption(_ + "\n" + _).map("\n" + _).getOrElse("")
        case None => ""
      }
    }) + "\n"

  def expect(entry: String, expectedResult: String) = assertEquals(expectedResult, printRaces(analysis))
  def expect(expectedResult: String): Unit = expect(testName.getMethodName() + "()V", expectedResult)
  def expectNoRaces: Unit = expectNoRaces(testName.getMethodName() + "()V")
  def expectNoRaces(entry: String) = expect(entry, "\n\n")
  def expectSomeRaces = assertNotSame("\n\n", printRaces(analysis))
}