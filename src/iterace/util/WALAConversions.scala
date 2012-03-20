package iterace.util

import com.ibm.wala.classLoader.IMethod
import com.ibm.wala.ipa.callgraph.CGNode
import com.ibm.wala.classLoader.IField
import com.ibm.wala.ssa.SSAInstruction
import com.ibm.wala.ipa.callgraph.CallGraph
import scala.collection.JavaConversions._
import com.ibm.wala.ipa.callgraph.propagation.LocalPointerKey
import com.ibm.wala.ipa.callgraph.propagation.PointerKey
import com.ibm.wala.ipa.callgraph.propagation.InstanceKey
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.ipa.callgraph.Context
import com.ibm.wala.classLoader.IClass
import com.ibm.wala.util.strings.Atom
import com.ibm.wala.types.Descriptor
import com.ibm.wala.ipa.cfg.BasicBlockInContext
import com.ibm.wala.ssa.analysis.IExplodedBasicBlock
import com.ibm.wala.ssa.SSAGetInstruction
import com.ibm.wala.types.FieldReference
import com.ibm.wala.ssa.SSAPutInstruction
import com.ibm.wala.classLoader.ShrikeBTMethod
import com.ibm.wala.ipa.callgraph.propagation.AllocationSiteInNode
import com.ibm.wala.ssa.SSAInvokeInstruction
import com.ibm.wala.ssa.SSAPhiInstruction
import com.ibm.wala.ssa.IR
import com.ibm.wala.util.intset.IntSet
import com.ibm.wala.util.intset.IntSetAction

class WALAConversions extends TypeAliases with WALAConversionsForN with WALAConversionsForP {
  trait Named {
    def name(): String
  }

  implicit def imethod2named(m: IMethod): Named = new Named {
    def name() = m.getSelector().getName().toString()
  }

  implicit def c2named(c: IClass): Named = new Named {
    def name = c.getName().getClassName().toString()
  }

  trait PrettyPrintable {
    def prettyPrint(): String
  }

  implicit def m2prettyprintable(m: IMethod): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = {
      val packageName = m.getDeclaringClass().getName().getPackage().toString().replace('/', '.')
      packageName + "." + m.getDeclaringClass().name + "." + m.name
    }
  }

  // method
  object M {
    def unapply(m: M): Option[(IClass, String)] = {
      Some(m.getDeclaringClass(), m.getSelector().toString())
    }
  }

  // class, iclass
  object C {
    def unapply(c: C): Option[(String, String)] = {
      Some(c.getName().getPackage().toString(), c.getName().getClassName().toString())
    }
  }

  def printCodeLocation(n: N, bytecodeIndex: Int): String = {
    printCodeLocation(n.getMethod(), bytecodeIndex)
  }

  implicit def mWithLineNo(m: M) = new {
    def lineNoFromBytecodeIndex(bytecodeIndex: Int) = m match {
      case m:ShrikeBTMethod => m.getLineNumber(bytecodeIndex)
      case _ => -1
    }
    def lineNoFromIRNo(irNo: Int) = lineNoFromBytecodeIndex(m.asInstanceOf[ShrikeBTMethod].getBytecodeIndex(irNo))
  }

  def printCodeLocation(m: IMethod, bytecodeIndex: Int): String = {
    val lineNo = m.lineNoFromBytecodeIndex(bytecodeIndex)
    val className = m.getDeclaringClass().getName().getClassName().toString()
    "" + m.prettyPrint + "(" + className + ".java:" + lineNo + ")"
  }

  implicit def iWithEasyUses(i: I) = new {
    def uses: Iterable[V] = Stream.range(0, i.getNumberOfUses()).map(index => { i.getUse(index) })
  }

  implicit def o2prettyprintable(o: O): PrettyPrintable = new PrettyPrintable {
    def prettyPrint(): String = O.prettyPrint(o)
  }

  implicit def intsetTraversable(s: IntSet) = new Traversable[Int] {
    def foreach[U](f: Int => U) = {
      s.foreach(new IntSetAction() {
        override def act(x: Int) = f(x)
      })
    }
  }

  //  	public static String variableName(Integer v, CGNode cgNode,
  //			int ssaInstructionNo) {
  //		String[] localNames;
  //		try {
  //			localNames = cgNode.getIR().getLocalNames(ssaInstructionNo,
  //					v);
  //		} catch (Exception e) {
  //			localNames = null;
  //		} catch (UnimplementedError e) {
  //			localNames = null;
  //		} 
  //		String variableName = null;
  //		if (localNames != null && localNames.length > 0)
  //			variableName = localNames[0];
  //		return variableName;
  //	}

  // access instructions of Some(object, field, is_write)
  //  object AccessI {
  //    def unapply(i: I):Option[(F, V)] = {
  //      i match {
  //        case rI: SSAGetInstruction => Some(rI.getDeclaredField(), rI.getUse(0))
  //        case wI: SSAPutInstruction => Some(wI.getDeclaredField(), wI.getDef())
  //        case _ => None
  //      }
  //    }
  //  }
}

object WALAConversions extends WALAConversions