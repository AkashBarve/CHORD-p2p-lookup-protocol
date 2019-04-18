# CHORD-p2p-lookup-protocol
Project for COP5618 Concurrent Programming, Spring 2019

##### Group members and Github ids:
1. Akash Barve (AkashBarve)
2. Nanda Kishore Sunki (nandakishore15)
3. Swarabarna Sarkar (Swarabarna)

##### Contributions:

##### Descrirption of the project:

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
    4. To run "sbt run <Number of nodes to start> <Number of requests to generate>"
    eg. sbt run 1000 10
    5. You can also use "sbt compile run <Number of nodes to start> <Number of requests to generate>"
    eg. sbt compile run 1000 10"

Note: The sbt version and library compatibilities might sometimes cause a error saying some dependency could not be compiled. 
To solve this you can run the project on an sbt shell on IntelliJ

2. To Run the project in IntelliJ:

    
    1. After opening; File -> New -> Project from existing sources -> Navigate to project 
    folder (this folder has src, build.sbt and README.md umder it)  
    2. Click the + button and select SBT Task.
    3. Name it 'Run the program'.
    4. In the Tasks field, type ~run. The ~ causes SBT to rebuild and rerun the project when you save changes to a file in the project.
    5. Click OK.
    6. On the Run menu. Click Run ‘Run the program’.



             