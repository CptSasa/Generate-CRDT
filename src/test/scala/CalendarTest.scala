import kofre.base.Lattice
import kofre.base.Lattice.Operators
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.Prop.{forAll, forAllNoShrink, forAllShrink, lzy}
import org.scalacheck.{Arbitrary, Gen, Properties, Shrink}
import pprint.PPrinter

import java.util.UUID
import util.chaining.scalaUtilChainingOps
import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.Properties

class CalendarTest {}
  object CalendarSpecification extends Properties("Calendar") {
    val calendarHandler = new CalendarHandler

    implicit def shrinkerWithLzy: Shrink[List[CalendarOperation]] = Shrink { trace =>
      trace.toStream.flatMap(c => c match
        case Add(num) => {
          Stream(trace.diff(List(c)))
        }
        case Remove(num) => {
          Stream(trace.diff(List(c)))
        }
        case Merge(other) => {
          Stream(trace.diff(List(c))).
            concat(shrinkerWithLzy.shrink(other).
              filter(_.nonEmpty).map(x => trace.updated(trace.indexOf(c), Merge(x))))
        })
    }
    //    implicit def traceShrink: Shrink[List[CalOp]] = Shrink { trace =>
    //      val possiblestates = listAllPossibleOptions(trace)
    //      //println("hi from shrinker!")
    //      possiblestates.toStream //.filter(x => calendar.constructCalendar(Calendar(Dotted.empty,UUID.randomUUID().toString), x).sum()> 30)
    //    }

    def listAllPossibleOptions(list: List[CalendarOperation]): List[List[CalendarOperation]] = {
      val possibleStates = new ListBuffer[List[CalendarOperation]]
      for (n <- list) {
        n match
          case Add(num) => possibleStates.addOne(list.diff(List(n)))
          case Remove(num) => possibleStates.addOne(list.diff(List(n)))
          case Merge(other) => {
            possibleStates.addOne(list.diff(List(n)))
            val subStates = listAllPossibleOptions(other)
            for (x <- subStates) {
              if (x.nonEmpty) {
                possibleStates.addOne(list.diff(List(n)).appended(Merge(x)))
                //possibleStates.addOne(list.updated(list.indexOf(n),MergeOp(x)))
              }
            }
          }
      }
      possibleStates.toList
    }
    val calendar = Calendar(Dotted.empty, UUID.randomUUID().toString)
    def generateClass: Gen[List[CalendarOperation]] = calendarHandler.generateListOfOp(5)

    implicit def opGenerator: Arbitrary[List[CalendarOperation]] = Arbitrary(generateClass)

    val traceOfOneCalFixed = forAll(generateClass) { (generatedTrace: List[CalendarOperation]) =>
      val generatedCalender = calendarHandler.constructCalendar(calendar, generatedTrace)
      generatedCalender.sum() < 31
    }
    property("testCalendar") = traceOfOneCalFixed
      //    traceOfOneCalFixed.useSeed("sum32",Some("rVMHDhhvXVpTHLLL-ZUt2M9e7XAbKBYpi9iQJ9eK6lE="))
      def reduceList(prettyList: List[CalendarOperation]): List[CalendarOperation] = {
        if (prettyList.size == 1) {
          prettyList.head match
            case Add(num) => List{Add(num)}
            case Remove(num) => List{Remove(num)}
            case Merge(other) => reduceList(other)
        }
        else {
          prettyList
        }
      }
      val greenPrinter = pprint.copy(
        colorLiteral = fansi.Color.Green
      )
    val redPrinter = pprint.copy(
      colorLiteral = fansi.Color.Red
    )
      def prettyPrintList(listToPrint: List[CalendarOperation],cal: Calendar): Unit = {
        if (listToPrint.size > 0) {
          val nextCalendar = calendarHandler.constructCalendar(cal, List {
            listToPrint.head
          })
          if (nextCalendar.sum() < 30) {
            greenPrinter.pprintln(listToPrint.head)
            prettyPrintList(listToPrint.tail, nextCalendar)
          }
          else {
            redPrinter.pprintln(listToPrint.head)
            prettyPrintList(listToPrint.tail, nextCalendar)
          }
        }
      }
      prettyPrintList(reduceList(List(Merge(List(Merge(List(Merge(List(Add(21))), Merge(
        List (Add(28))))
    ) ) ))),new Calendar(Dotted.empty,UUID.randomUUID().toString))
//      reduceList(minimizedList)
  }

