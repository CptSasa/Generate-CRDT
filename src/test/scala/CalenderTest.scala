import kofre.base.Lattice
import kofre.base.Lattice.Operators
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.Prop.forAll
import org.scalacheck.{Gen, Properties}

class CalenderTest {}

object CalenderSpecification extends Properties("Calender") {

  val calender = Calender(Dotted.empty)

  def generateViaTrace(trace : List[op]): Calender ={
    var cal = Calender(Dotted.empty)
    for(n<-trace){
      cal = n.functionToCall(cal)
    }
    return cal
  }
  /*
  val holdsRes = forAll(calender.generateCalenderWithTrace(), calender.generateCalenderWithTrace()) { (x: (Calender,List[op]), y: (Calender,List[op])) =>
    var tmp = x._1
    tmp = tmp.mergeCal(x._1, y._1)
    //System.out.println(cal.toString)
    tmp.holdsRestriction() && tmp.sum()<=30
  }
*/

  val validTrace = forAll(calender.generateTrace(),calender.generateTrace())  { (x: (List[op]), y: (List[op])) =>
    val x2 = generateViaTrace(x)
    val y2 = generateViaTrace(y)
    val tmp = x2.mergeCal(x2, y2)
    for (n <- x) {
     // System.out.print(n.prettyPrint)
    }
    tmp.holdsRestriction() && tmp.sum() <= 30
  }
  val holdsResWithList = forAll(calender.generateCalenderWithTrace(), calender.generateCalenderWithTrace()) { (x: (Calender,List[op]), y: (Calender,List[op])) =>
    var tmp = x._1
    tmp = tmp.mergeCal(x._1,y._1)
    for(n<- x._2){
     // System.out.print(n.prettyPrint)
    }
    //System.out.println("")
    val x2 = generateViaTrace(x._2)
    val y2 = generateViaTrace(y._2)
    val combined = x2.mergeCal(x2,y2)
    tmp.holdsRestriction() && tmp.sum()<=30
  }
  property("holdsResWork") = validTrace
  property("holdsRes") = holdsResWithList
}

