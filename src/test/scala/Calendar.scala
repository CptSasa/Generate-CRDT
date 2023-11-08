import kofre.base.Lattice
import kofre.base.Lattice.{Operators, merge}
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll
import java.util.UUID
import scala.collection.mutable.ListBuffer

sealed trait CalendarOperation
case class Add(num: Int) extends CalendarOperation
case class Remove(num: Int) extends CalendarOperation
case class Merge(other: List[CalendarOperation]) extends CalendarOperation

case class Calendar (calendarList: Dotted[AddWinsSet[Int]],replicaID : String) {

  def sum(): Int = {
    calendarList.elements.sum
  }

  //constructs a calender with a given calender and a trace that contains operations which need to be called opon the Calendar

  def addCal(value: Int): Calendar = {
    //System.out.println(calendar.sum() + " : "+ value+ " Add")
    val cal = this.copy()
    //var tmp = cal.calendarList
    if (cal.sum() + value <= 30) {
      return Calendar(cal.calendarList.add(using (cal.replicaID).asId)(value), cal.replicaID)
    }
    return cal

  }

  def removeCalendar( value: Int): Calendar = {
    //System.out.println(calendar.sum() + " : "+ value+ " Remove")
    val cal = this.copy()
    val tmp = cal.calendarList.remove(value)
    return Calendar(tmp, cal.replicaID)
  }

  def mergeCalendar(calendar2: Calendar): Calendar = {
    val cal = this.copy()
    //System.out.println(calendar.sum() + " Merge "  + calendar2.sum())
    return Calendar(cal.calendarList merge calendar2.calendarList, this.replicaID)
  }

}
class CalendarHandler {
  def generateChoiceWithFrequency(depth: Int): Int = {
    val weightedChoices: Gen[Int] = Gen.frequency(
      (5, 0),
      (5, 1),
      (depth, 2)
    )
    weightedChoices.sample.get
  }

  def generateInt(): Int = {
    return Gen.choose(1, 29).sample.get
  }

  def constructCalendar(current: Calendar, remainingTrace: List[CalendarOperation]): Calendar = {
    if (remainingTrace.nonEmpty) {
      return remainingTrace.head match
        case Add(num) =>
          constructCalendar(current.addCal(num), remainingTrace.tail)
        case Remove(num) =>
          constructCalendar(current.removeCalendar(num), remainingTrace.tail)
        case Merge(other) =>
          constructCalendar(current.mergeCalendar(
            constructCalendar(Calendar(Dotted.empty, UUID.randomUUID().toString), other)), remainingTrace.tail)
    }
    else {
      return current
    }
  }

  //  def generateCalendar(depth: Int) : List[CalOp] = {
  //   }
  def generateCalOp(depth: Int): CalendarOperation = {
    return generateChoiceWithFrequency(depth) match
      case 0 => Add(generateInt())
      case 1 => Remove(generateInt())
      case 2 => Merge(generateListOfOp(depth - 1))
  }

  def generateListOfOp(depth: Int): List[CalendarOperation] = {
    val list = new ListBuffer[CalendarOperation]
    for (n <- 0 until Gen.choose(depth, 5 + depth).sample.get) {
      list.addOne(generateCalOp(depth))
    }
    return list.toList
  }
}

