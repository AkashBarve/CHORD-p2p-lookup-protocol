import java.util.concurrent.ThreadLocalRandom
import scala.concurrent.duration.FiniteDuration
import java.util.concurrent.TimeUnit
import scala.concurrent.ExecutionContext.Implicits.global

import akka.actor.{Actor, ActorRef}

case class sendExistingObj()
case class setExistingObj(receivedObj: ChordNode)
case class sendObj()
case class setObj(receivedObj: ChordNode, nodeId: Int)
case class updateOthersFingerTable()
case class join(joiningNodeId: Int, knownNode: ActorRef, networkNodes: Array[ActorRef])
case class updateKeys()
case class updateFinger(affectedNodes: List[Int], updatedValue: Int)
case class setPredecessor(predecessor : Int)
case class setSuccessor(successor : Int)
case class setFingerTable(fingertable : Array[String])
case class setNodeKeys(predecessor : Int)
case class getFingerTable()
case class reqFromNode(minKey : Int, maxKey : Int, nodes : Array[ActorRef])
case class findKey(key: Int, nodeOfOrigin: Int, hopCount: Int)
case class requestDone(totalHops: Int)
/*
Class for Chord Node actor.
 */
class ChordNode(Id: Int, numNodes: Seq[Int], M: Int, numReq : Int, HopCalcActor : ActorRef) extends Actor {
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
  var existNode: ActorRef = null
  var nodeSpace: Int = math.pow(2, M).toInt
  var existNodeObj: ChordNode = null
  var nodesObj: Array[ChordNode] = Array.ofDim[ChordNode](nodeSpace)
  var KeyRange :  Array[Int] = new Array[Int](2)
  var calcHops:ActorRef = HopCalcActor
  var nodeFetch : Array[ActorRef] = Array.ofDim[ActorRef](math.pow(2, M).toInt)
  var fingerTableChordIdentifier = Array.ofDim[Int](M)
  var fingerTableNodeVal = Array.ofDim[Int](M)

  def receive = {
    case reqFromNode(minKey, maxKey, nodes) => {
      //generating key to lookup
      var key = ThreadLocalRandom.current().nextInt(minKey, maxKey + 1)
      println("Running Lookup for key " + key)
      //fetches ActorRef of other nodes
      this.nodeFetch = nodes
      self ! findKey(key, nodeId, 0)
    }
     /*
      * requestDone retrieves the hopcount after the end of lookup
      */

    case requestDone(hopCount: Int) => {
      calcHops ! getHops(hopCount)
    }
    //implementation of the lookup protocol
    case findKey(key: Int, nodeOfOrigin: Int, hopCount: Int) => {
      var startNode = nodeOfOrigin
      //Every time a node is contacted for lookup, it is considered a hop
      var newHopCount = hopCount + 1

      if(newHopCount > 10) {
        self ! requestDone(newHopCount)
      }
      else {
        //checks if key is between the current node's predecessor and current node's id. if yes, lookup is done!
        if (key >= this.predecessor + 1 && key <= this.nodeId) {
          try {
            self ! requestDone(newHopCount)
          }
          catch {
            case e: NullPointerException => println("Faulty Node")
          }

        }//checks if the key can directly be reached using the current node's finger table
        else if (fingerTableChordIdentifier.contains(key)) {
          try {
            nodeFetch(fingerTableNodeVal(fingerTableChordIdentifier.indexOf(key))) ! findKey(key, startNode, newHopCount)
          }
          catch {
            case e: NullPointerException => self ! requestDone(newHopCount)
          }
        } else {
          //else a cyclic logic is run to find the key. we retrieve the closest preceding node and run the same logic for that node.
          if (checkForCycle(key, fingerTableChordIdentifier(M - 1), fingerTableChordIdentifier(0))) {
            try{
              nodeFetch(fingerTableNodeVal(M - 1)) ! findKey(key, startNode, newHopCount)
            }
            catch {
              case e: NullPointerException => self ! requestDone(newHopCount)
            }
          }
          for (i <- 0 to M - 2) {
            if (checkForCycle(key, fingerTableChordIdentifier(i), fingerTableChordIdentifier(i + 1))) {
              try{
                nodeFetch(fingerTableNodeVal(i + 1)) ! findKey(key, startNode, newHopCount)
              }
              catch {
                case e: NullPointerException => self !requestDone(newHopCount)
              }
            }
          }
        }
      }


    }
    //method to set the node's predecessor
    case setPredecessor(predecessor) => {
      this.predecessor = predecessor
      println("predecessor for " + nodeId + " is " + this.predecessor)
    }
      // method to set the node's successor
    case setSuccessor(successor) => {
      this.successor = successor
      println("successor for " + nodeId  +" is " + this.successor);
    }
      //Method to set the fingertable for each node
    case setFingerTable(fingertable) => {
      for (index <- 0 to M-1) {
        //the left handside of the fingertable containing chord identifier
        this.fingerTableChordIdentifier(index) = fingertable(index).split(",")(0).toInt
        //the right handside of the fingertable containing the correspoding Node
        this.fingerTableNodeVal(index) = fingertable(index).split(",")(1).toInt
        //println("*********" + fingerTableChordIdentifier(index) + "****" + fingerTableNodeVal(index))
      }
     //this.fingerTable = fingertable
      //println("fingertable for " + nodeId + " is of length" + fingerTable.size)
//      for(i <- 0 to this.fingerTable.size - 1) {
//        var printnode =  this.fingerTable(i).split(",")
//        println(nodeId + " + " + math.pow(2,i) + ", " + printnode(1))
//      }
    }
    case sendExistingObj() => {
      sender ! setExistingObj(this)

    }
    case setExistingObj(rObj: ChordNode) => {
      existNodeObj = rObj
    }
    case sendObj() => {
      sender ! setObj(this, nodeId)

    }
    case setObj(rObj: ChordNode, id: Int) => {
      nodesObj(id) = rObj
    }
    //stores the min and max key of the node
    case setNodeKeys(predecessor) => {
      this.KeyRange(0) = predecessor + 1
      this.KeyRange(1) = nodeId
    }


    case join(joinId: Int, existingNode: ActorRef, allNodes: Array[ActorRef]) => {
      nodeId = joinId
      existNode = existingNode
      m = M
      networkNodes = allNodes
      //fetch the existing node objects
      existNode ! sendExistingObj()
      //generating fingertable for the new node
      for (i <- 0 to m - 1) {
        var start = (nodeId + math.pow(2, i).toInt) % math.pow(2, m).toInt
        fingerTable(i) = (start + ",X")
      }

      for (i <- 0 to networkNodes.length - 1) {
        if (networkNodes(i) != null) {
          networkNodes(i) ! sendObj()
        }

      }
      println("case join for "+ nodeId)
      context.system.scheduler.scheduleOnce(FiniteDuration(3000, TimeUnit.MILLISECONDS), self, updateOthersFingerTable())

    }
    //this case updates the fingerTable for nodes affected by the join
    case updateOthersFingerTable() => {
      println("update others finger table for node "+nodeId)
      var affectedNodes: List[Int] = null
      if (this.nodeId < this.predecessor) {
        affectedNodes = List()
        for (i <- this.predecessor + 1 to math.pow(2, m).toInt - 1) {
          affectedNodes ::= i
        }
        for (i <- 0 to this.nodeId) {
          affectedNodes ::= i
        }
      } else {
        affectedNodes = List()
        for (i <- this.predecessor + 1 to this.nodeId) {
          affectedNodes ::= i
        }
      }

      for (i <- 0 to networkNodes.length - 1) {
        if (networkNodes(i) != null) {
          networkNodes(i) ! updateFinger(affectedNodes, nodeId)
        }

      }

      context.system.scheduler.scheduleOnce(FiniteDuration(3000, TimeUnit.MILLISECONDS), self, updateFinger(affectedNodes,this.nodeId))

    }

    //this case updates the entries in the finger table
    case updateFinger(nodesAffected: List[Int], newValue: Int) => {
      //first fetch the LHS and RHS of the finger table
      for( index <- 0 to m-1) {
        fingerTableStart(index) = fingerTable(index).split(",")(0).toInt
        fingerTableNode(index) = fingerTable(index).split(",")(1).toInt
      }
      //set the values for the index of the affected nodes with the new node ID on the RHS
      for (index <- 0 to nodesAffected.length-1) {
        if (fingerTableStart.contains(nodesAffected(index))) {
          fingerTable(fingerTableStart.indexOf(nodesAffected(index))) = fingerTable(fingerTableStart.indexOf(nodesAffected(index))).split(",")(0) + "," + newValue
        }
      }
      //set it back on the fingerTable
      for (index <- 0 to m-1) {
        fingerTableStart(index) = fingerTable(index).split(",")(0).toInt
        fingerTableNode(index) = fingerTable(index).split(",")(1).toInt
      }

    }

  }
  //cyclic check for lookup
  def checkForCycle(nodeID: Int, first: Int, second: Int): Boolean = {
    if (first < second) {
      if (nodeID > first && nodeID < second) {
        return true
      }
      else {
        return false
      }
    } else {
      if (nodeID > first || nodeID < second) {
        return true
      }
      else {
        return false
      }
    }
  }

}
