package old
import CalenderSpecification.calender
import kofre.base.Lattice
import kofre.base.Lattice.{Operators, merge}
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.{Arbitrary, Gen}
import org.scalacheck.Prop.forAll

import java.util
import scala.collection.mutable.ListBuffer


/*trait Operation {
  var operationName: operation
  //def functionToCall : Calender=>Calender
  var value: Any
}
*/
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
  override def toString = ""+ operationName + " " + value //+ "\n"
  def prettyPrint = "" + operationName + ": "+ value+ "-> "

  def printSubOp(toPrint: op): String = {
    if (toPrint.operationName == operation.Merge){
      var sub=  "" + operationName+ " " + "\n"+ " "
      for(n<- toPrint.value.asInstanceOf[List[op]]){
        sub = sub.++(printSubOp(n))
      }
      return sub
    }
    else{
      return ""+ operationName + " " + value + "\n"
    }
  }
}

case class Calender(calList: Dotted[AddWinsSet[Int]])  {

  def sum(): Int = {
    calList.elements.sum
  }
  def generateChoiceWithFrequency(depth : Int): Int ={
    val weightedChoices: Gen[Int]= Gen.frequency(
      (5, 0),
      (1, 1),
      (depth, 2) //default == 3 -> jede Operation hat die gleiche Wahrscheinlichkeit ausgeführt zu werden
    )
    weightedChoices.sample.get
  }
  def returnFittingOp(num: Int,depth: Int): op= {
    val func = new op
    val generatedValue = Gen.choose(0, 20).sample.get
    if(num == 0){
      val func = new op
      func.value = generatedValue
      def convertedAddCal(calender: Calender) = addCal(calender, func.value.asInstanceOf[Int])
      func.functionToCall = convertedAddCal(_)
      func.operationName = operation.Add
      return func
    }
    if (num == 1) {
      val func = new op
      func.value = generatedValue
      def convertRemoveCal(calender: Calender) = removeCal(calender, func.value.asInstanceOf[Int])
      func.functionToCall = convertRemoveCal(_)
      func.operationName = operation.Remove
      return func
    }
    if (num == 2) {
      val func = new op
      func.value = generateTrace(depth)
      def convertMergeCal(calender: Calender) = mergeCal(calender, generateViaTrace(func.value.asInstanceOf[List[op]]))
      func.functionToCall = convertMergeCal(_)
      func.operationName = operation.Merge
      return func
    }
    return func

  }
  def generateOp(depth : Int) : Gen[op] = {
    val chooseOp : Gen[op] = Gen.frequency(
      (5, returnFittingOp(0,depth)),
      (5, returnFittingOp(1,depth)),
      (depth, returnFittingOp(2,depth)))
    return chooseOp
  }
  //generates a op which contains a function with generated parameters
  def generateOperation(depth : Int): op = {
    //eventuell statt List[Calender=>Calender],Trait und später den trait rausfinden = Funktion als invalid anzeigen
    val generatedValue = generateSizedInt().sample.get
    val func = new op

    //choose which function is getting called based on the chosen Number

    val chosenFunction = generateChoiceWithFrequency(depth)
    func.value = chosenFunction match {
      case (0) => generatedValue
      case (1) => generatedValue
      case (2) => generateTrace(depth-1)//generateOperation(depth-1)
    }

    def convertedAddCal(calender: Calender) = addCal(calender, func.value.asInstanceOf[Int])
    def convertRemoveCal(calender: Calender) = removeCal(calender, func.value.asInstanceOf[Int])
    def mergeCalender(calender: Calender) = mergeCal(calender,generateViaTrace(func.value.asInstanceOf[List[op]]))

    func.functionToCall = chosenFunction match {
      case (0) => convertedAddCal(_)
      case (1) => convertRemoveCal(_)
      case (2) => mergeCalender(_)
      case (_) => null
    }
    //function Name based on the chosen number
    func.operationName = chosenFunction match {
      case (0) => operation.Add
      case (1) => operation.Remove
      case (2) => operation.Merge
      case (_) => operation.UnKnownFunction
    }

    return func
  }
  //returns a generated Calender with a Trace that contains every operation whicch has been called on that Calender
  def generateCalenderWithTrace(): (Calender,List[op]) = {
    var cal = Calender(Dotted.empty)
    var trace = ListBuffer[op]()
    for (n <- 0 until Gen.choose(0,2).sample.get){
      var op = generateOperation(Gen.choose(0,5).sample.get)
      trace.addOne(op)
      cal = generateOperation(Gen.choose(0,5).sample.get).functionToCall(cal)
    }
    return (cal,trace.toList)
  }

  def generateViaTrace(trace: List[op]): Calender = {
    var generatedCalender = Calender(Dotted.empty)
    for (n <- trace) {
      if(n.operationName == operation.Merge){
        generatedCalender = mergeCal(generatedCalender,generateViaTrace(n.value.asInstanceOf[List[op]]))
      }
      else {
        if(n.operationName == operation.Add){
          generatedCalender = addCal(generatedCalender,n.value.asInstanceOf[Int])
        }
        generatedCalender = n.functionToCall(generatedCalender)
      }
    }
    return generatedCalender
  }

  def generateTrace(depth : Int): (List[op]) = {
    var trace = ListBuffer[op]()
    for (n <- 0 until Gen.choose(depth,depth+3).sample.get) {
      var op = generateOperation(depth)
      trace.addOne(op)
    }
    return trace.toList
  }
  def generateGenerator(): Gen[Calender]={
    val customGenerator : Gen[Calender] = generateViaTrace(generateTrace(5))
    return customGenerator
  }

  def generateTraceGenerator(): Gen[List[op]] = {
    val customGenerator: Gen[List[op]] = generateTrace(2)
    return customGenerator
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
    return Calender(calender.calList merge calender2.calList)
  }

  def generateSizedInt(): Gen[Int] = {
    val gener = Gen.choose(0, 30)
    gener
  }
  def functionList(): List[(Calender, Int) => Calender] = List(removeCal(_,_),addCal(_,_),addRemainingDays(_,_))

  def holdsRestriction(): Boolean = sum()<30
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