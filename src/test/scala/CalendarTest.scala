import kofre.base.Lattice
import kofre.base.Lattice.Operators
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.Prop.{forAll, forAllNoShrink, forAllShrink, lzy}
import org.scalacheck.{Arbitrary, Gen, Properties, Shrink}

import java.util.UUID
import util.chaining.scalaUtilChainingOps
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.Properties

class CalendarTest {}
  object CalendarSpecification extends Properties("Calendar") {
    val calendar = new CalendarHandler
     implicit def shrinkerWithLzy: Shrink[List[CalOp]] = Shrink { trace =>
      trace.toStream.flatMap(c => c match
        case AddOp(num) =>{
          Stream(trace.diff(List(c)))
        }
        case RemoveOp(num) => {
          Stream(trace.diff(List(c)))
        }
        case MergeOp(other) =>{
            Stream(trace.diff(List(c))).concat(shrinkerWithLzy.shrink(other).filter(_.nonEmpty).map(x => trace.diff(List(MergeOp(other))).appended(MergeOp(x))))
        })
    }
//    implicit def traceShrink: Shrink[List[CalOp]] = Shrink { trace =>
//      val possiblestates = listAllPossibleOptions(trace)
//      //println("hi from shrinker!")
//      possiblestates.toStream //.filter(x => calendar.constructCalendar(Calendar(Dotted.empty,UUID.randomUUID().toString), x).sum()> 30)
//    }

    def listAllPossibleOptions(list: List[CalOp]): List[List[CalOp]] = {
      val possibleStates = new ListBuffer[List[CalOp]]
      for (n <- list) {
        n match
          case AddOp(num) => possibleStates.addOne(list.diff(List(n)))
          case RemoveOp(num) => possibleStates.addOne(list.diff(List(n)))
          case MergeOp(other) => {
            possibleStates.addOne(list.diff(List(n)))
            val subStates = listAllPossibleOptions(other)
            for (x <- subStates) {
              if (x.nonEmpty) {
                possibleStates.addOne(list.diff(List(n)).appended(MergeOp(x)))
                //possibleStates.addOne(list.updated(list.indexOf(n),MergeOp(x)))
              }
            }
          }
      }
      possibleStates.toList
    }

    def generateClass: Gen[List[CalOp]] = calendar.generateListOfOp(9)

    implicit def opGenerator: Arbitrary[List[CalOp]] = Arbitrary(generateClass)

    val traceOfOneCalFixed = forAll(opGenerator.arbitrary) { (generatedTrace: List[CalOp]) =>
      val generatedCalender = calendar.constructCalendar(Calendar(Dotted.empty,UUID.randomUUID().toString), generatedTrace)
      //            System.out.println(generatedTrace)
//                  System.out.println(generatedCalender.sum())
      generatedCalender.sum() <= 30
    }


    traceOfOneCalFixed.check()


}

