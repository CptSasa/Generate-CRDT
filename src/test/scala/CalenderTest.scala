import CalenderSpecification.calender
import kofre.base.Lattice
import kofre.base.Lattice.Operators
import kofre.datatypes.AddWinsSet
import kofre.syntax.ReplicaId
import kofre.base.Uid.asId
import kofre.dotted.Dotted
import org.scalacheck.Prop.forAll
import org.scalacheck.{Arbitrary, Gen, Properties, Shrink}

import scala.collection.immutable.List
import scala.collection.mutable.ListBuffer
import scala.language.postfixOps
import scala.util.Properties

class CalenderTest {}

object CalenderSpecification extends Properties("Calender") {

  val calender = Calender(Dotted.empty)
  def generateFixedTrace(): List[op] ={
    val operate = new op
    operate.value = 15
    operate.operationName = operation.Add
    var tmpCal = calender.addCal(calender,15)
    def convertedAddCal(calender: Calender) = calender.addCal(calender, operate.value.asInstanceOf[Int])
    operate.functionToCall = convertedAddCal(_)
    val operateAdd14 = new op
    operateAdd14.value = 14
    operateAdd14.operationName = operation.Add
    tmpCal = calender.addCal(calender, 14)
    def convertedAddCal14(calender: Calender) = calender.addCal(calender, operate.value.asInstanceOf[Int])
    operateAdd14.functionToCall = convertedAddCal14(_)

    val operateAdd5 = new op
    operateAdd5.value = 5
    operateAdd5.operationName = operation.Add
    tmpCal = calender.addCal(calender, 5)
    def convertedAddCal5(calender: Calender) = calender.addCal(calender, operate.value.asInstanceOf[Int])
    operateAdd14.functionToCall = convertedAddCal5(_)

    val operateMerge = new op
    operateMerge.value = List(operateAdd5)
    operateMerge.operationName = operation.Merge
    tmpCal = calender.mergeCal(calender, calender.generateViaTrace(operateMerge.value.asInstanceOf[List[op]]))
    def convertedMerge(calender: Calender) = calender.mergeCal(calender,tmpCal)
    operateMerge.functionToCall = convertedMerge(_)

    val operateAdd2 = new op
    operateAdd2.value = 2
    operateAdd2.operationName = operation.Add
    tmpCal = calender.addCal(calender, 2)

    def convertedAddCal2(calender: Calender) = calender.addCal(calender, operate.value.asInstanceOf[Int])

    operateAdd14.functionToCall = convertedAddCal5(_)

    val list = new ListBuffer[op]
    list.addOne(operate)
    list.addOne(operateAdd14)
    list.addOne(operateMerge)
    list.addOne(operateAdd2)
    return list.toList
  }
  /*
  val holdsRes = forAll(calender.generateCalenderWithTrace(), calender.generateCalenderWithTrace()) { (x: (Calender,List[op]), y: (Calender,List[op])) =>
    var tmp = x._1
    tmp = tmp.mergeCal(x._1, y._1)
    //System.out.println(cal.toString)
    tmp.holdsRestriction() && tmp.sum()<=30
  }
*/
  object OperationShrinker{
    def customOperationShrinker: Shrink[op] = Shrink { operationToReduce =>
      if(operationToReduce.value == operation.Merge){
        operationToReduce.value = operationToReduce.value.asInstanceOf[List[op]].filter(_.operationName != operation.Remove)
      }
      Stream(operationToReduce)
    }
  }
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
          if (calender.generateViaTrace(operationToReduce.filter(_ != n)).sum() > 30) {
            operationToReduce = operationToReduce.filter(_ != n)
          }
        }
      }
      /*
      for(n <- operationToReduce){
        var counter = 0
        while(counter<operationToReduce.size) {
          if(calender.generateViaTrace(operationToReduce).sum() > 30){
             if(n.operationName ==operation.Merge){
               if(calender.mergeCal(calender.generateViaTrace(operationToReduce.filter(_ != n)),calender.generateViaTrace(n.value.asInstanceOf[List[op]].filter(_ != n.value.asInstanceOf[List[op]](counter)))).holdsRestriction()){
                 val newOp = new op
                 newOp.value = n.value.asInstanceOf[List[op]].filter(_ != n.value.asInstanceOf[List[op]](counter))
                 newOp.operationName = operation.Merge
                 def mergeCalender(calender: Calender) = calender.mergeCal(calender,calender.generateViaTrace(newOp.value.asInstanceOf[List[op]]))
                 newOp.functionToCall= mergeCalender(_)
                 operationToReduce = operationToReduce.updated(operationToReduce.indexOf(n),newOp)
               }
             }
        }
          counter = counter+1
      }
      }*/
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


  implicit def traceShrink: Shrink[List[op]] = Shrink { trace =>
    TraceShrinker.minimizingShrinker.shrink(trace)
  }

//  implicit val operationShrinker: Shrink[op] = Shrink { operate =>
//    OperationShrinker.customOperationShrinker.shrink(operate)
//  }


/*
    implicit val calenderShrink: Shrink[Calender] = Shrink { calender =>
      TraceShrinker.calenderShrinker.shrink(calender)
    }
*/
    val validTrace = forAll(calender.generateTrace(2), calender.generateTrace(2)) { (x: (List[op]), y: (List[op])) =>
      val x2 = calender.generateViaTrace(x)
      //System.out.println(x2.sum())
      val y2 = calender.generateViaTrace(y)
      val tmp = x2.mergeCal(x2, y2)
      tmp.holdsRestriction() && tmp.sum() <= 30
    }

  val validList = forAll(calender.generateTrace(2), calender.generateTrace(2)) { (x: (List[op]), y: (List[op])) =>
    val x2 = calender.generateViaTrace(x)
    //System.out.println(x2.sum())
    val y2 = calender.generateViaTrace(y)
    val tmp = x2.mergeCal(x2, y2)
    x2.holdsRestriction() && x2.sum() <= 30
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
    /*
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
    */

  //implicit def opGen: Arbitrary[List[op]] = Arbitrary(generateClass)
  def generateClass: Gen[List[op]] = generateFixedTrace()

  val traceOfOneCal = forAll(generateClass) { (generatedTrace: List[op]) =>
    val generatedCalender = calender.generateViaTrace(generatedTrace)
    generatedCalender.holdsRestriction() && generatedCalender.sum() <= 30
  }
/*
    generateListOfGeneratedValue.check()
    generateTraceGenerator.check()
    generateCalenderWithArbitrary.check
*/
    traceOfOneCal.check()
    validTrace.check()
    validList.check()
  //traceOfOneCal.check()
    property("holdsResWork") = traceOfOneCal
    //property("generateOneCal") = traceOfOneCal
    //property("holdsRes") = holdsResWithList
    property("GeneratorTrace") = generatorTrace
    //property("generateTraceGenerator") = generateTraceGenerator
    //property("generater") = generateListOfGeneratedValue.++(generateTraceGenerator)



}

