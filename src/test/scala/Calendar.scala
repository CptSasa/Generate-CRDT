import kofre.base.Lattice
import kofre.base.Lattice.{Operators, merge}
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll

import scala.collection.mutable.ListBuffer

sealed trait CalOp
case class AddOp(num: Int) extends CalOp
case class RemoveOp(num: Int) extends CalOp
case class MergeOp(other: List[CalOp]) extends CalOp

case class Calendar (calendarList: Dotted[AddWinsSet[Int]]) {

  def sum(): Int = {
    calendarList.elements.sum
  }

  def constructCalendar(current: Calendar, remainingTrace: List[CalOp]) : Calendar = {

    if(remainingTrace.nonEmpty){
    return remainingTrace.head match
      case AddOp(num) => constructCalendar(current.addCal(current,num), remainingTrace.tail)
      case RemoveOp(num)=> constructCalendar(current.removeCalendar(current,num),remainingTrace.tail)
      case MergeOp(other)=> constructCalendar(current.mergeCalendar(current,constructCalendar(Calendar(Dotted.empty),other)),remainingTrace.tail)
  }
    else{
    return current
    }
  }

//  def generateCalendar(depth: Int) : List[CalOp] = {
//   }
  def generateCalOp(depth: Int): CalOp ={
  return generateChoiceWithFrequency(depth) match
  case 0 => AddOp(generateInt())
  case 1 => RemoveOp(generateInt())
  case 2 => MergeOp(generateListOfOp(depth-1))
}
  def generateListOfOp(depth: Int): List[CalOp] ={
    val list =new ListBuffer[CalOp]
    for (n <- 0 until Gen.choose(depth,5+depth).sample.get){
      list.addOne(generateCalOp(depth))
  }
    return list.toList
}
  def addCal(calendar: Calendar, value: Int): Calendar = {
    val cal = calendar.copy()
    var tmp = cal.calendarList
    if (calendar.sum() + value <= 30) {
      tmp = cal.calendarList.add(using ("" + System.currentTimeMillis()).asId)(value)
    }
    return Calendar(tmp)

  }

  def removeCalendar(calendar: Calendar, value: Int): Calendar = {
    val cal = calendar.copy()
    val tmp = cal.calendarList.remove(value)
    return Calendar(tmp)
  }

  def mergeCalendar(calendar: Calendar, calendar2: Calendar): Calendar = {
    return Calendar(calendar.calendarList merge calendar2.calendarList)
  }

  def generateChoiceWithFrequency(depth: Int): Int = {
    val weightedChoices: Gen[Int] = Gen.frequency(
      (3, 0),
      (3, 1),
      (depth, 2) //default == 3 -> jede Operation hat die gleiche Wahrscheinlichkeit ausgeführt zu werden
    )
    weightedChoices.sample.get
  }
  def generateInt(): Int ={
    return Gen.choose(1,29).sample.get
  }
}
