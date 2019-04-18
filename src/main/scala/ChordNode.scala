import java.util.concurrent.ThreadLocalRandom

import akka.actor.{Actor, ActorRef}

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
case class reqFromNode(minKey : Int, maxKey : Int, nodes : Array[ActorRef])
case class findKey(key: Int, nodeOfOrigin: Int, hopCount: Int)
case class requestDone(totalHops: Int)

class ChordNode(Id: Int, numNodes: Seq[Int], M: Int, numReq : Int, HopCalcActor : ActorRef) extends Actor {
  var nodeId: Int = 0
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
  var calcHops:ActorRef = HopCalcActor
  var nodeFetch : Array[ActorRef] = Array.ofDim[ActorRef](math.pow(2, M).toInt)
  var fingerTableChordIdentifier = Array.ofDim[Int](M)
  var fingerTableNodeVal = Array.ofDim[Int](M)

  def receive = {
    case reqFromNode(minKey, maxKey, nodes) => {
      var key = ThreadLocalRandom.current().nextInt(minKey, maxKey + 1)
      println("Running Lookup for key" + key)
      this.nodeFetch = nodes
      println("nodeFetch" + nodeFetch(5))
      self ! findKey(key, nodeId, 0)
    }

    case requestDone(hopCount: Int) => {
      calcHops ! getHops(hopCount)
    }

    case findKey(key: Int, nodeOfOrigin: Int, hopCount: Int) => {
      var startNode = nodeOfOrigin
      var newHopCount = hopCount + 1

      println("value of m "+ m + " " + fingerTable)
      if(newHopCount > 10) {
        self ! requestDone(newHopCount)
      }
      else {

        if (key >= this.predecessor + 1 || key <= this.nodeId) {
          try {
            println("I am" + this.nodeId + "searching for " + key + "in if")
            self ! requestDone(newHopCount)
          }
          catch {
            case e: NullPointerException => println("Faulty Node")
          }

        } else if (fingerTableChordIdentifier.contains(key)) {
          println("I am" + this.nodeId + "searching for " + key + "in else if")
          try {
            nodeFetch(fingerTableNodeVal(fingerTableChordIdentifier.indexOf(key))) ! findKey(key, startNode, newHopCount)
          }
          catch {
            case e: NullPointerException => self ! requestDone(newHopCount)
          }
        } else {
          if (checkForCycle(key, fingerTableChordIdentifier(M - 1), fingerTableChordIdentifier(0))) {
            try{
              nodeFetch(fingerTableNodeVal(M - 1)) ! findKey(key, startNode, newHopCount)
            }
            catch {
              case e: NullPointerException => self ! requestDone(newHopCount)
            }
          }
          for (i <- 0 to M - 2) {
            println("I am" + this.nodeId + "searching for " + key + "in else")
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

    case setPredecessor(predecessor) => {
      this.predecessor = predecessor
      println("predecessor for " + nodeId + " is " + this.predecessor)
    }
    case setSuccessor(successor) => {
      this.successor = successor
      println("successor for " + nodeId  +" is " + this.successor);
    }
    case setFingerTable(fingertable) => {
      for (index <- 0 to M-1) {
        this.fingerTableChordIdentifier(index) = fingertable(index).split(",")(0).toInt
        this.fingerTableNodeVal(index) = fingertable(index).split(",")(1).toInt
        println("*********" + fingerTableChordIdentifier(index) + "****" + fingerTableNodeVal(index))
      }
     //this.fingerTable = fingertable
      //println("fingertable for " + nodeId + " is of length" + fingerTable.size)
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
      //context.system.scheduler.scheduleOnce(FiniteDuration(3000, TimeUnit.MILLISECONDS), self, updateOthersFingerTable())

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

      //context.system.scheduler.scheduleOnce(FiniteDuration(3000, TimeUnit.MILLISECONDS), self, updateKeys())

    }
    case updateFinger(nodesAffected: List[Int], newValue: Int) => {
      for( index <- 0 to m-1) {
        fingerTableStart(index) = fingerTable(index).split(",")(0).toInt
        fingerTableNode(index) = fingerTable(index).split(",")(1).toInt
      }

      for (index <- 0 to nodesAffected.length-1) {
        if (fingerTableStart.contains(nodesAffected(index))) {
          fingerTable(fingerTableStart.indexOf(nodesAffected(index))) = fingerTable(fingerTableStart.indexOf(nodesAffected(index))).split(",")(0) + "," + newValue
        }
      }

      for (index <- 0 to m-1) {
        fingerTableStart(index) = fingerTable(index).split(",")(0).toInt
        fingerTableNode(index) = fingerTable(index).split(",")(1).toInt
      }

    }
    case updateKeys() => {
      //println("TODO: update keys")
      //TODO

    }

  }

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
