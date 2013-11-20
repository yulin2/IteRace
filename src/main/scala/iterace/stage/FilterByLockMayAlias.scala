package iterace.stage
import iterace.pointeranalysis.RacePointerAnalysis
import iterace.datastructure.ProgramRaceSet
import iterace.datastructure.Lock
import iterace.datastructure.LockSets
import iterace.datastructure.Race
import iterace.datastructure.MayAliasLockConstructor
import iterace.IteRaceOption
import iterace.pointeranalysis.Loop

/**
 * read: "filter by locks based on may-alias"
 */
class FilterByLockMayAlias(pa: RacePointerAnalysis, lockMapping: LockSets) extends Stage {
  import pa._

  def apply(races: ProgramRaceSet): ProgramRaceSet = {

    /*
     * this was defined in S 
     * 
    	// mutable but not part of the object's identity
		var lockset: Option[Set[Lock]] = None 
     */

    new ProgramRaceSet(races.children map (loopRaceSet => {
      loopRaceSet.l match {
        case l: Loop =>
          val locks = lockMapping.getLocks(l)
          val lockMap = lockMapping.getLockSetMapping(l)

          def isSafe(r: Race): Boolean = {
            val aLocks = lockMap(r.a); //r.a.lockset = Option(aLocks)
            val bLocks = lockMap(r.b); //r.b.lockset = Option(bLocks)

            (aLocks & bLocks).size > 0
          }
          loopRaceSet.filter { !isSafe(_) }

        case _ => loopRaceSet
      }
    }) filter { _.size > 0 })
  }
}