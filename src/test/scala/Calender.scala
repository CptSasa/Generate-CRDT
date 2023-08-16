import CalenderSpecification.calender
import kofre.base.Lattice
import kofre.base.Lattice.{Operators, merge}
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.Gen
import org.scalacheck.Prop.forAll

import java.util
import scala.collection.mutable.ListBuffer

trait Operation {
  var operationName: operation
  //def functionToCall : Calender=>Calender
  var value: Any
}
  enum operation{
    case Add,Remove, Merge,UnKnownFunction
  }

class op {
  //Operationname Name of the Operation
  var operationName = operation.UnKnownFunction
  //Generated function to call
  var functionToCall = (calender: Calender) => calender
  //Value of the function
  var value: Any = 0
  override def toString = ""+ operationName + " " + value
  def prettyPrint = "" + operationName + ": "+ value+ "-> "
}


case class Calender(calList: Dotted[AddWinsSet[Int]])  {

  def sum(): Int = {
    calList.elements.sum
  }
  //generates a op which contains a function with generated parameters
  def generateOperation: op = {
    //eventuell statt List[Calender=>Calender],Trait und später den trait rausfinden = Funktion als invalid anzeigen
    val generatedValue = generateSizedInt().sample.get
    val func = new op
    func.value = generatedValue

    def convertedAddCal(calender: Calender) = addCal(calender, generatedValue)
    def convertRemoveCal(calender: Calender) = removeCal(calender, generatedValue)
    def mergeCalender(calender: Calender) = mergeCal(calender,generateViaTrace(List(generateOperation)));
    //choose which function is getting called based on the chosen Number
    val chosenFunction = Gen.choose(0, 1).sample.get
    func.functionToCall = chosenFunction match {
      case (0) => convertedAddCal(_)
      case (1) => convertRemoveCal(_)
      case (0) => mergeCalender(_)
      case (_) => null
    }
    //function Name based on the chosen number
    func.operationName = chosenFunction match {
      case (0) => operation.Add
      case (1) => operation.Remove
      case (_) => operation.UnKnownFunction
    }
    return func
  }
  //returns a generated Calender with a Trace that contains every operation whicch has been called on that Calender
  def generateCalenderWithTrace(): (Calender,List[op]) = {
    var cal = Calender(Dotted.empty)
    var trace = ListBuffer[op]()
    for (n <- 0 until Gen.choose(0,15).sample.get){
      var op = generateOperation
      trace.addOne(op)
      cal = generateOperation.functionToCall(cal)
    }
    return (cal,trace.toList)
  }

  def generateViaTrace(trace: List[op]): Calender = {
    var cal = Calender(Dotted.empty)
    for (n <- trace) {
      cal = n.functionToCall(cal)
    }
    return cal
  }

  def generateTrace(): (List[op]) = {
    var cal = Calender(Dotted.empty)
    var trace = ListBuffer[op]()
    for (n <- 0 until Gen.choose(0, 15).sample.get) {
      var op = generateOperation
      trace.addOne(op)
      cal = generateOperation.functionToCall(cal)
    }
    return trace.toList
  }

  def mergeTraces(listA: Set[op], listB: Set[op]): Set[op] ={
    var mergedTrace = listA
    val merge = new op
    merge.value = 0
    merge.operationName = operation.Merge
    listA.+(merge)
    listB.foreach(x => mergedTrace.+(x))
    return mergedTrace
  }

  def addCal(calender: Calender, value: Int): Calender = {
    val cal = calender.copy()
    var tmp = cal.calList
    if (calender.sum() + value <= 30) {
      tmp = cal.calList.add(using ("" + System.currentTimeMillis()).asId)(value)
    }
    return Calender(tmp)
  }

  def addRemainingDays(calender: Calender, value: Int): Calender = {
    val cal = calender.copy()
    var tmp = cal.calList
    if (calender.sum() + value <= 30) {
      val remainingDays = 30 - cal.sum()
      tmp = cal.calList.add(using ("" + System.currentTimeMillis()).asId)(remainingDays)
    }
    return Calender(tmp)
  }

  def removeCal(calender: Calender, value: Int): Calender = {
    val cal = calender.copy()
    val tmp = cal.calList.remove(value)
    return Calender(tmp)
  }

  def mergeCal(calender: Calender, calender2: Calender): Calender = {
    val a = calender.calList merge calender2.calList
    return Calender(a)
  }

  def generateSizedInt(): Gen[Int] = {
    val gener = Gen.choose(0, 30)
    gener
  }
  def functionList(): List[(Calender, Int) => Calender] = List(removeCal(_,_),addCal(_,_),addRemainingDays(_,_))

  def holdsRestriction(): Boolean = sum()<=30
  /*
  def generateProperty(): Property={
    val validTrace = forAll(calender.generateTrace(), calender.generateTrace()) { (x: (List[op]), y: (List[op])) =>
      val x2 = generateViaTrace(x)
      val y2 = generateViaTrace(y)
      val tmp = x2.mergeCal(x2, y2)
      for (n <- x) {
        System.out.print(n.prettyPrint)
      }
      tmp.holdsRestriction() && tmp.sum() <= 30
    }
  }
  */
  override def toString: String = calList.elements.toString()
}