import akka.actor.{ActorSystem, Props}

object Main extends App {
  if (args.length != 2) {
    println("Two arguments needed")
  }
  else {
    try {
      val numNodes = args(0).toInt
      val numReq = args(1).toInt
      val system = ActorSystem("ChordSystem") //define ActorSystem to get things started
      // default Actor constructor
      val chordActor = system.actorOf(Props(new ChordActor(numNodes, numReq)), name = "chordactor") //actorof creates actor instance
      //now that we have instance of an actor send message to simulate Chord network
      chordActor ! "startNetwork"
    }
    catch {
      case e: NumberFormatException => {
        println("Number of nodes and requests should be in integer format")
        System.exit(1)
      }
    }



  }

}

