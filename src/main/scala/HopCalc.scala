import akka.actor.Actor

case class getHops(hopCount: Int)

/*
Actor to aggregate hopcounts for each request made in the network.
It is inititalized with totalRequests that it expects to be generated in the network.
 */
class HopCalc(totalRequests: Int) extends Actor {
    var totalHops: Int = 0
    var requestsReceived: Int = 0
    
    def receive = {
        //getHops takes the hopCount for each request, adds it to totalHops.
        //When it receives all requests, it calculate the average hops and prints it.
        case getHops(hopCount: Int) => {
            totalHops += hopCount
            requestsReceived += 1

            if (requestsReceived == totalRequests) {
                //println(totalHops)
                //println(requestsReceived)
                var averageHops: Double = totalHops.toDouble/requestsReceived
                println("Total hops received is " + totalHops)

                println("Average hops for each request is " + averageHops)
            }
        }
    }
}