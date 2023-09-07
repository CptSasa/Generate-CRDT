import kofre.base.Lattice
import kofre.base.Lattice.Operators
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Properties, Shrink}

import scala.collection.immutable.List
import scala.util.Properties

class CalenderTest {}

object CalenderSpecification extends Properties("Calender") {

  val calender = Calender(Dotted.empty)
  /*
  val holdsRes = forAll(calender.generateCalenderWithTrace(), calender.generateCalenderWithTrace()) { (x: (Calender,List[op]), y: (Calender,List[op])) =>
    var tmp = x._1
    tmp = tmp.mergeCal(x._1, y._1)
    //System.out.println(cal.toString)
    tmp.holdsRestriction() && tmp.sum()<=30
  }
*/
 object TraceShrinker {
    def customShrinker: Shrink[List[op]] = Shrink { operationToReduce =>
      var filteredList = operationToReduce.filter(_.operationName != operation.Remove)
      filteredList = filteredOperations(filteredList)
      filteredList = filteredList.filter{case (opObject) => opObject.operationName == operation.Add && opObject.value.asInstanceOf[Int] > 10 ||opObject.operationName == operation.Merge}
//      for (n <- filteredList) {
//        if (calender.generateViaTrace(filteredList).sum() > 30) {
//          if(calender.generateViaTrace(filteredList.filter(_.value != n.value)).sum()>30){
//            filteredList = filteredList.filter(_ != n)
//          }
//        }
//      }
      Stream(filteredList)
      //Shrink.shrink(filteredList)
    }

    def minimizingShrinker: Shrink[List[op]] = Shrink { toReduce =>
      var operationToReduce = toReduce
      for (n <- operationToReduce) {
        if (calender.generateViaTrace(operationToReduce).sum() > 30) {
          if (calender.generateViaTrace(operationToReduce.filter(_.value != n.value)).sum() > 30) {
            operationToReduce = operationToReduce.filter(_.value != n.value)
          }
        }
      }
        var counter = operationToReduce.size
        while(counter>0) {
          if(calender.generateViaTrace(operationToReduce).sum() > 30){
             if(calender.generateViaTrace(operationToReduce.filter(_.value == operationToReduce(counter-1).value)).sum()>30){
               operationToReduce = operationToReduce.filter(_.value == operationToReduce(counter-1).value)
             }
        }
          counter = counter-1
      }
      Stream(operationToReduce)
      //Shrink.shrink(filteredList)
    }
//    def removeOneLowestElementFromTree(operationTree: op): op ={
//      if(operationTree.operationName == operation.Merge){
//
//      }
//      else{
//        return operationTree
//      }
//    }

    def filteredOperations(filteredList: List[op]): List[op] = {
      filteredList.map { operationToReduce =>
        if (operationToReduce.operationName == operation.Merge) {
          val tmp = new op
          tmp.operationName = operation.Merge
          tmp.functionToCall = operationToReduce.functionToCall
          var test = filteredOperations(operationToReduce.value.asInstanceOf[List[op]].filter{case (opObject) => (opObject.operationName == operation.Add && opObject.value.asInstanceOf[Int] > 10) ||opObject.operationName == operation.Merge})
          tmp.value = test
          tmp
        }
        else {
          operationToReduce // Keep the original OperationToReduce unchanged
        }
      }
    }

    /*
    val calenderShrinker: Shrink[Calender] = Shrink{ calender =>
      val dummyCal = calender
      val seconddummy = new Calender(Dotted.empty)
      if(calender.sum()>30){
        dummyCal.addCal(dummyCal,24);
        seconddummy.addCal(seconddummy,23)
        dummyCal.mergeCal(dummyCal,seconddummy)
      }
      Stream(dummyCal)

    */
  }


  implicit val traceShrink: Shrink[List[op]] = Shrink { trace =>
    TraceShrinker.customShrinker.shrink(trace)
  }


/*
    implicit val calenderShrink: Shrink[Calender] = Shrink { calender =>
      TraceShrinker.calenderShrinker.shrink(calender)
    }
*/
    val validTrace = forAll(calender.generateTrace(4), calender.generateTrace(4)) { (x: (List[op]), y: (List[op])) =>
      val x2 = calender.generateViaTrace(x)
      val y2 = calender.generateViaTrace(y)
      val tmp = x2.mergeCal(x2, y2)
      tmp.holdsRestriction() && tmp.sum() <= 30
    }
    val holdsResWithList = forAll(calender.generateCalenderWithTrace(), calender.generateCalenderWithTrace()) { (x: (Calender, List[op]), y: (Calender, List[op])) =>
      var tmp = x._1
      tmp = tmp.mergeCal(x._1, y._1)
      //System.out.println("")
      val x2 = calender.generateViaTrace(x._2)
      val y2 = calender.generateViaTrace(y._2)
      val combined = x2.mergeCal(x2, y2)
      tmp.holdsRestriction() && tmp.sum() <= 30
    }
    val generatorTrace = forAll(calender.generateGenerator()) { (x: Calender) =>
      x.holdsRestriction() && x.sum() <= 30
    }
    implicit def opGen: Arbitrary[List[op]] = Arbitrary(generateClass)
    def generateClass: Gen[List[op]] = calender.generateTrace(4)

    def opGenerate: Gen[op] = calender.generateOp(4)
    def list: Gen[List[op]] = Gen.listOfN(10, opGenerate)
    implicit def opGenArbit: Arbitrary[List[op]] = Arbitrary(list)

    def genCal: Gen[Calender] = calender.generateViaTrace(list.sample.get)
    implicit def opCalenderArbit: Arbitrary[Calender] = Arbitrary(genCal)

    val generateTraceGenerator = forAll(opGen.arbitrary) { x =>
      val generatedCalender = calender.generateViaTrace(x)
      generatedCalender.holdsRestriction() && generatedCalender.sum() <= 30
    }
    val generateListOfGeneratedValue = forAll(opGenArbit.arbitrary) { (x: List[op]) =>
      val generatedCalender = calender.generateViaTrace(x)
      generatedCalender.holdsRestriction() && generatedCalender.sum() <= 30
    }
    val generateCalenderWithArbitrary = forAll(opCalenderArbit.arbitrary, opCalenderArbit.arbitrary) { (x: Calender, y: Calender) =>
      val tmp = x.mergeCal(x, y)
      //System.out.println(tmp.toString)
      tmp.holdsRestriction() && tmp.sum() <= 30
    }

    val traceOfOneCal = forAll(generateClass){ (generatedTrace: List[op]) =>
      val generatedCalender = calender.generateViaTrace(generatedTrace)
      generatedCalender.holdsRestriction() && generatedCalender.sum() <= 30
    }

/*
    generateListOfGeneratedValue.check()
    generateTraceGenerator.check()
    generateCalenderWithArbitrary.check
*/
    validTrace.check()
    traceOfOneCal.check()
    property("holdsResWork") = validTrace
    property("generateOneCal") = traceOfOneCal
    //property("holdsRes") = holdsResWithList
    property("GeneratorTrace") = generatorTrace
    property("generateTraceGenerator") = generateTraceGenerator
    property("generater") = generateListOfGeneratedValue.++(generateTraceGenerator)



}

