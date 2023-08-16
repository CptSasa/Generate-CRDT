import kofre.base.Lattice
import kofre.base.Lattice.Operators
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.{Dotted}



val set: Dotted[AddWinsSet[Int]] = Dotted.empty
val secondset: Dotted[AddWinsSet[Int]] = Dotted.empty

val a = set.add(using "me".asId)(5)
val b = set.add(using "you".asId)(10)
val bb = set.add(using "you".asId)(15)
val v = secondset.add(using "d".asId)(15)

val c = set.remove(10) // removals do not need a replica ID, they remove all known instances of the value

val result = (a merge b merge bb)

case class Calender(calList: Dotted[AddWinsSet[Int]]){
  def sum(): Int = {
    calList.elements.sum
  }
  override def toString: String = calList.elements.toString()

}

def addCal(calender: Calender, value: Int): Calender = {
  val cal = calender.copy()
  var tmp = cal.calList
  if (calender.sum() + value <= 30) {
    tmp = cal.calList.add(using ("" + System.currentTimeMillis()).asId)(value)
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
def functionList(): List[(Calender, Int) => Calender] = {
  val list = List(removeCal(_, _), addCal(_, _))
  return list
}

val cal = Calender(secondset)
val functionCall = functionList()(1)
//val functionlist = List(addCal(_,_),removeCal)
val cal2 = functionCall(cal,10)
val cal3 = addCal(cal,20)
val calResult = mergeCal(cal2,cal3)
val calResult2 = addCal(calResult,11)

def generateCalender(): Calender ={
  var cal = Calender(Dotted.empty)
  for(n <- 0 until 14){
    if(n%3 == 0){
      cal = addCal(cal,n)
    }
    else{
      cal = removeCal(cal,4)
    }
  }
  return cal
}
System.out.print(generateCalender().toString)
System.out.print(generateCalender().sum())

System.out.print(calResult2.toString)
System.out.print(calResult2.toString)
System.out.print(calResult2.sum())
//val cal3 = cal.addCal(cal2,10)
//cal = cal.removeCal(cal,10)
System.out.print(result.elements)
