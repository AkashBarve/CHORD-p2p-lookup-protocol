import java.util.concurrent.{ThreadLocalRandom, TimeUnit}

import akka.actor.{Actor, ActorRef}

import scala.concurrent.duration.FiniteDuration
import scala.concurrent.ExecutionContext.Implicits.global

case class getKnownObj()
case class setKnownObj(receivedObj: ChordNode)
case class getObj()
case class setObj(receivedObj: ChordNode, nodeId: Int)
case class updateOthersFingerTable()
case class join(joiningNodeId: Int, knownNode: ActorRef, networkNodes: Array[ActorRef], numRequests: Int)
case class updateKeys()
case class updateFinger(affectedNodes: List[Int], updatedValue: Int)
case class setPredecessor(predecessor : Int)
case class setSuccessor(successor : Int)
case class setFingerTable(fingertable : Array[String])
case class setNodeKeys(predecessor : Int)
case class getFingerTable()
case class reqFromNode(minKey : Int, maxKey : Int)
case class findKey(key: Int, nodeOfOrigin: Int)

class ChordNode(Id: Int, numNodes: Seq[Int], M: Int, numReq : Int) extends Actor {
  var nodeId: Int = Id
  //println("***************************")
  println("creating node " + nodeId)
  var successor: Int = 0
  var predecessor: Int = 0
  var fingerTable = Array.ofDim[String](M)
  var m: Int = M
  var allKeys: List[Int] = List()
  var numRequests: Int = numReq
  var fingerTableStart = Array.ofDim[Int](M)
  var fingerTableNode = Array.ofDim[Int](M)
  var networkNodes: Array[ActorRef] = null
  var knownNode: ActorRef = null
  var nodeSpace: Int = math.pow(2, M).toInt
  var knownNodeObj: ChordNode = null
  var nodesObj: Array[ChordNode] = Array.ofDim[ChordNode](nodeSpace)
  var KeyRange :  Array[Int] = new Array[Int](2)
  //override def receive: Receive = ???

  def receive = {
    case reqFromNode(minKey, maxKey) => {
      var key = ThreadLocalRandom.current().nextInt(minKey, maxKey + 1)
      self ! findKey(key, nodeId)
    }

    case findKey(key, nodeOfOrigin) => {

    }


    case setPredecessor(predecessor) => {
      this.predecessor = predecessor
      println("predecessor for " + nodeId + " is " + this.predecessor)
    }
    case setSuccessor(successor) => {
      this.successor = successor
      println("successor for " + nodeId  +" is " + this.successor);
    }
    case setFingerTable(fingertable) => {
     this.fingerTable = fingertable
      println("fingertable for " + nodeId + " is of length" + fingerTable.size)
//      for(i <- 0 to this.fingerTable.size - 1) {
//        var printnode =  this.fingerTable(i).split(",")
//        println(nodeId + " + " + math.pow(2,i) + ", " + printnode(1))
//      }
    }
    case setNodeKeys(predecessor) => {
      this.KeyRange(0) = predecessor + 1
      this.KeyRange(1) = nodeId
    }
    case join(joinId: Int, kNode: ActorRef, allNodes: Array[ActorRef], requestNumber: Int) => {
      nodeId = joinId
      knownNode = kNode
      m = M
      networkNodes = allNodes
      numRequests = requestNumber
      knownNode ! getKnownObj()
      for (i <- 0 to m - 1) {
        var start = (nodeId + math.pow(2, i).toInt) % math.pow(2, m).toInt
        fingerTable(i) = (start + ",X")
      }

      for (i <- 0 to networkNodes.length - 1) {
        if (networkNodes(i) != null) {
          networkNodes(i) ! getObj()
        }

      }
      println("case join for "+ nodeId)
      context.system.scheduler.scheduleOnce(FiniteDuration(3000, TimeUnit.MILLISECONDS), self, updateOthersFingerTable())

    }
    case getKnownObj() => {
      sender ! setKnownObj(this)

    }
    case setKnownObj(rObj: ChordNode) => {
      knownNodeObj = rObj
    }
    case getObj() => {
      sender ! setObj(this, nodeId)

    }
    case setObj(rObj: ChordNode, id: Int) => {
      nodesObj(id) = rObj
    }

    case updateOthersFingerTable() => {
      println("update others finger table for node "+nodeId)
      var Nodes: List[Int] = null
      if (this.nodeId < this.predecessor) {
        Nodes = List()
        for (i <- this.predecessor + 1 to math.pow(2, m).toInt - 1) {
          Nodes ::= i
        }
        for (i <- 0 to this.nodeId) {
          Nodes ::= i
        }
      } else {
        Nodes = List()
        for (i <- this.predecessor + 1 to this.nodeId) {
          Nodes ::= i
        }
      }

      for (i <- 0 to networkNodes.length - 1) {
        if (networkNodes(i) != null) {
          networkNodes(i) ! updateFinger(Nodes, nodeId)
        }

      }

      context.system.scheduler.scheduleOnce(FiniteDuration(3000, TimeUnit.MILLISECONDS), self, updateKeys())

    }
    case updateFinger(aNodes: List[Int], newValue: Int) => {
      //println("TODO: update finger")
      //TODO

    }
    case updateKeys() => {
      //println("TODO: update keys")
      //TODO

    }

  }

}
