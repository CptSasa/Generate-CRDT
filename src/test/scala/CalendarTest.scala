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
    val calendar = Calendar(Dotted.empty)

    implicit def traceShrink: Shrink[List[CalOp]] = Shrink { trace =>
      val possiblestates = listAllPossibleOptions(trace)
      println("hi from shrinker!")
      possiblestates.toStream
//      var reducedTrace = trace
//      for (n <- trace) {
//        if (calendar.constructCalendar(Calendar(Dotted.empty), reducedTrace).sum() > 30) {
//          if(calendar.constructCalendar(Calendar(Dotted.empty), reducedTrace.filter(_ != n)).sum()> 30){
//            reducedTrace = reducedTrace.filter(_ != n)
//          }
//        }
//      }
    }
//    implicit def shrinking(): Shrink[List[CalOp]] = Shrink { trace =>
//      listAllPossibleOptions(trace).toStream
//    }
////    for (n <- operationToReduce) {
////      if (calender.generateViaTrace(operationToReduce).sum() > 30) {
////        System.out.println(calender.generateViaTrace(operationToReduce.filter(_.value != n.value)).sum())
////        if (calender.generateViaTrace(operationToReduce.filter(_.value != n.value)).sum() > 30) {
////          operationToReduce = operationToReduce.filter(_.value != 2)
////        }
////      }
////    }
def testConstructCalender(): List[CalOp] = {
  List(MergeOp(List(AddOp(10),MergeOp(List(AddOp(12),AddOp(13))))),AddOp(11),MergeOp(List(AddOp(2),AddOp(14))))
}
//
//    implicit def shrinkOperation(): Shrink[CalOp] = Shrink { operation =>
//      operation match
//        case AddOp(num) => toStream(operation)
//        case RemoveOp(num) => operation
//        case MergeOp(other) => MergeOp(Shrink.shrink(other))
//  }

    def listAllPossibleOptions (list: List[CalOp]): List[List[CalOp]] =
    {
        System.out.println("using this")
        val possibleStates = new ListBuffer[List[CalOp]]
        for (n <- list) {
          n match
            case AddOp(num) => possibleStates.addOne(list.diff(List(n)))
            case RemoveOp(num) => possibleStates.addOne(list.diff(List(n)))
            case MergeOp(other) => {
              possibleStates.addOne(list.diff(List(n)))
              possibleStates.addOne(list.diff(List(n)).appended(MergeOp(other.tail)))
              }
              //System.out.println("Possible State :" + possibleStates)

        }
      possibleStates.toList
          //List.concat(possibleStates.toList,listAllPossibleOptions(possibleStates.toList.head))
  }
          def generateFixed: Gen[List[CalOp]] = testConstructCalender()
          def generateClass: Gen[List[CalOp]] = calendar.generateListOfOp(3)

          implicit def opGeneratorFixed: Arbitrary[List[CalOp]] = Arbitrary(generateClass)

          val traceOfOneCalFixed = forAll(generateClass) { (generatedTrace: List[CalOp]) =>
            val generatedCalender = calendar.constructCalendar(calendar, generatedTrace)
            System.out.println(generatedTrace)
            System.out.println(generatedCalender.sum())
            generatedCalender.sum() <= 30
          }
          val fixed = forAll(generateFixed){ (generatedTrace: List[CalOp]) =>
            System.out.println(generatedTrace)
            System.out.println(calendar.constructCalendar(calendar, generatedTrace).sum())
            System.out.println(calendar.constructCalendar(calendar, generatedTrace).calendarList)
            calendar.constructCalendar(Calendar(Dotted.empty), generatedTrace).sum() <= 30

          }
          traceOfOneCalFixed.check()
          //fixed.check()

      }
object Hello {
  def main(args: Array[String]) = {
//   val testList = List(MergeOp(List(RemoveOp(27), AddOp(15), RemoveOp(20))), RemoveOp(9), MergeOp(List(RemoveOp(28), RemoveOp(28), RemoveOp(7), RemoveOp(14), RemoveOp(11))), AddOp(16), RemoveOp(19), MergeOp(List(AddOp(8), AddOp(9), AddOp(12), MergeOp(List(AddOp(19), RemoveOp(26))), MergeOp(List(AddOp(7), RemoveOp(16), AddOp(26))), RemoveOp(25))), MergeOp(List(AddOp(23), RemoveOp(17), MergeOp(List(RemoveOp(22), AddOp(17), RemoveOp(3), RemoveOp(21))), RemoveOp(29), AddOp(18))), RemoveOp(11))
    val testList = List(AddOp(16), MergeOp(List(AddOp(25), AddOp(3), AddOp(26), MergeOp(List(AddOp(6))), AddOp(15), MergeOp(List(MergeOp(List(AddOp(15), AddOp(7), AddOp(11), AddOp(24))), MergeOp(List(AddOp(7), AddOp(25), AddOp(19), AddOp(22))), AddOp(28), AddOp(12))), AddOp(28))))
    val cal = Calendar(Dotted.empty)
    val newCalendar = cal.constructCalendar(cal,testList)
    System.out.println(newCalendar.sum())
    System.out.println(newCalendar.calendarList)
  }
}

