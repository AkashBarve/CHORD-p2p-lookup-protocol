# CHORD-p2p-lookup-protocol
Project for COP5618 Concurrent Programming, Spring 2019

##### Group members and Github ids:
1. Akash Barve (AkashBarve)
2. Nanda Kishore Sunki (nandakishore15)
3. Swarabarna Sarkar (Swarabarna)

##### Contributions:
Akash Barve : Creation of Chord Actor, Nodes as Actors and generation of fingerTable, predecessor and successor by passing messages among Actors.
 Also, implementation of passing the key requests to the required Actor for lookup protocol.
Nanda Kishore Sunki : Implementation of the lookup protocol. Also, calculation of hopcount using Actors.
Swarabarna Sarkar : Addition of nodes to the network and updation of finger table affected by the add.

##### Description of the project:
Chord protocol provides method to lookup location of keys and their values in an efficient manner. It also has the ability to balance load and require less movement of keys when nodes join or leave the network.
The protocol is a fast distributed computation of a hash function mapping keys to nodes responsible for them. Chord assigns keys to nodes with consistent hashing, which has several desirable properties.
We have created each chord node as an actor. A Chord node needs only a small amount of routing information about other nodes. This information is stored in fingertable. Because this information is distributed, 
a node resolves the hash function by communicating with other nodes. Each actor or node in the network routes the request to the nearest possible node which is
present in it's fingertable. This helps is efficiently routing the request to its destination.
In an N-node network, each node maintains information about only O(log N ) other nodes, and a lookup requires O(log N ) messages.

##### Building and running the project:
Preferred versions:
JDK : 1.8.0
Scala: 2.11.0

1. To run the project from command line/ Terminal:
                    
     
    1. Install sbt on your computer.
    Mac : https://www.scala-sbt.org/release/docs/Installing-sbt-on-Mac.html
    Linux : https://www.scala-sbt.org/release/docs/Installing-sbt-on-Linux.html
    Windows : https://www.scala-sbt.org/release/docs/Installing-sbt-on-Windows.html 
    2. On your terminal move to the project directory ~/CHORD-p2p-lookup-protocol/
    3. To compile use command "sbt compile"
    4. To run sbt "run <Number of nodes to start> <Number of requests to generate> <Time to Stabilize Network (in secs)>"
    eg. sbt "run 30 10 10"
    5. You can also use sbt compile "run <Number of nodes to start> <Number of requests to generate> <Time to Stabilize Network (in secs)>"
    eg. sbt compile "run 30 10 10"

Note: The sbt version and library compatibilities might sometimes cause a error saying some dependency could not be compiled. 
To solve this you can run the project on an sbt shell on IntelliJ

2. To Run the project in IntelliJ:

    
    1. After opening; File -> New -> Project from existing sources -> Navigate to project 
    folder (this folder has src, build.sbt and README.md umder it)  
    2. Import project form external model as a sbt, and click Next.
    3. Select project jdk as java jdk version 1.8, and click Next.
    4. Build the program and wait for the program to finish building.
    5. Open an sbt shell. After the shell is initialized we can see "sbt:Chord-final". Then run
    the command "run <Number of nodes to start> <Number of requests to generate> <Time to Stabilize Network (in secs)>"
    eg. run 30 10 10



             