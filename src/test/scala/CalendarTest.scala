import kofre.base.Lattice
import kofre.base.Lattice.Operators
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.Prop.{forAll, forAllNoShrink, forAllShrink, lzy}
import org.scalacheck.{Arbitrary, Gen, Properties, Shrink}
import util.chaining.scalaUtilChainingOps
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.Properties

class CalendarTest {}
  object CalendarSpecification extends Properties("Calendar") {

    implicit def traceShrink: Shrink[List[CalOp]] = Shrink { trace =>
      println("hi from shrinker!")
      trace.toStream.map(o => trace.diff(List(o)).tap(println))
    }
    val calendar = Calendar(Dotted.empty)
    def generateClass: Gen[List[CalOp]] = calendar.generateListOfOp(4)
    implicit def opGeneratorFixed: Arbitrary[List[CalOp]] = Arbitrary(generateClass)
      val traceOfOneCalFixed = forAll(opGeneratorFixed.arbitrary) { (generatedTrace: List[CalOp]) =>
        val generatedCalender = calendar.constructCalendar(calendar,generatedTrace)
        System.out.println(generatedTrace)
        generatedCalender.sum() <= 30
      }
      traceOfOneCalFixed.check()
  }

