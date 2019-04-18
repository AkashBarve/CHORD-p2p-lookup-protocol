import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorRef, Props}

import scala.collection.mutable
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.duration.FiniteDuration
case class closeProgram()
case class StartRequests(nodeList : Seq[Int])

class ChordActor(numNodes: Int, numReq: Int) extends Actor {
  val M = Math.ceil(Math.log(numNodes) / Math.log(2.0)).toInt
  var nodes = Array.ofDim[ActorRef](math.pow(2, M).toInt)
  println(Math.pow(2,M))
  var requestKeyPool = Array.ofDim[ActorRef](numReq)
  var HopCalcActor:ActorRef = null


  override def receive: Receive = {
    case "startNetwork" =>
      val nodeIds = new mutable.HashSet[Int]()
      val joiningNodeIds = new mutable.HashSet[Int]()
      var nodeId = 0
      for (i <- 1 to numNodes) {
        do {
          nodeId = Hashify.getRandomId(M)
        } while (nodeIds.contains(nodeId))
        nodeIds.add(nodeId)
      }
      println("Initial Node Ids: "+ nodeIds)
      val sortedNodeIds = nodeIds.toSeq.sorted
      val minKey = sortedNodeIds(0);
      val maxKey = sortedNodeIds(numNodes - 1)
      HopCalcActor = context.system.actorOf(Props(new HopCalc(numReq*numNodes)))
      for (x <- sortedNodeIds) {
        var idx = sortedNodeIds.indexOf(x)
        nodes(sortedNodeIds(idx)) = context.system.actorOf(Props(new ChordNode(x, sortedNodeIds, M, numReq, HopCalcActor)))
        var predecessor = 0
        var successor = 0
        var fingertable = Array.ofDim[String](M)
        if(idx != 0 && idx != sortedNodeIds.size - 1
        ) {
          predecessor = sortedNodeIds(idx - 1)
          successor = sortedNodeIds(idx + 1)
        }
        else if(idx == 0) {
          successor = sortedNodeIds(idx + 1)
          predecessor = sortedNodeIds(sortedNodeIds.size - 1)
        }
        else {
          predecessor = sortedNodeIds(idx - 1)
          successor = sortedNodeIds(0)
        }

        for (i <- 0 to M-1) {
          var entry = (sortedNodeIds(idx) + math.pow(2, i).toInt) % math.pow(2, M).toInt
          var correspondingNode = 0
          if (sortedNodeIds.contains(entry)) {
            correspondingNode = entry
          } else if (entry > sortedNodeIds(sortedNodeIds.length - 1) || entry < sortedNodeIds(0)) {
            correspondingNode = sortedNodeIds(0)
          }
          else {
            for (c <- 0 to sortedNodeIds.size - 1) {
              if (entry > sortedNodeIds(c) && entry < sortedNodeIds(c + 1)) {
                correspondingNode = sortedNodeIds(c+1)
              }

            }

          }

          fingertable(i) = (entry + "," + correspondingNode)
          println("fingerval " + correspondingNode)

        }
        nodes(sortedNodeIds(idx)) ! setPredecessor(predecessor)
        nodes(sortedNodeIds(idx)) ! setSuccessor(successor)
        nodes(sortedNodeIds(idx)) ! setFingerTable(fingertable)
        nodes(sortedNodeIds(idx)) ! setNodeKeys(predecessor)


//        println("successor " + successor)
//        println("predecessor " + predecessor)
//
        //nodes += context.actorOf(Props(new ChordNode(x, sortedNodeIds, M)), x.toString)
        //nodes ! InitNode(x, sortedNodeIds)
      }
      println("Waiting for a few seconds to let the system stabilize.")
      context.system.scheduler.scheduleOnce(FiniteDuration(25, TimeUnit.SECONDS), self, StartRequests(sortedNodeIds))

    case StartRequests(nodeList) => {
      println("starting " + numReq + " requests...")
      println(nodeList.size)
      println("nodes" + nodes(5))
      for(i <- 0 to nodeList.size - 1) {
        {
          for (j <- 0 to numReq - 1) {
            nodes(nodeList(i)) ! reqFromNode(nodeList.min, nodeList.max, nodes)

          }
        }
      }
      }


    case closeProgram() =>
      context.system.shutdown()
  }
}

