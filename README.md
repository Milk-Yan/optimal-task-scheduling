# SOFTENG 306 Project 1 - Group 13 Notice me Sinnenpai
## Background
This project attempts to solve a difficult scheduling problem for the leader of a 'Big-As Parallel Computing Centre', who needs to schedule processes on their parallel computer systems. The objectives of the project are fast execution time and high quality software. More details and documentation about the project can be found in the [wiki](wiki/Home.md).

## Running the Project
There are two milestones for the project. There will be a GitHub release made for each milestone, including a runnable jar of the project.
To run the jar, use the following command:
```
java -jar scheduler.jar INPUT.dot P [OPTION]
```
* `INPUT.dot` a task graph with integer weights in dot format
* `P` number of processors to schedule the INPUT graph on

OPTIONAL:
* `-p N` use `N` cores for execution in parallel (default is sequential)
* `-v` visualise the search
* `-o OUTPUT` output file is named `OUTPUT` (default is INPUT-output.dot)

## Building and Compiling
Check that Java 1.8 is installed using the following command:
```
java -version
```
If it is not installed, you can install it on Linux from [here.](https://www.oracle.com/java/technologies/javase/javase-jdk8-downloads.html)

This is a [Maven](https://maven.apache.org/) Project. To check is Maven is installed, please run the following command:
```
maven -version
```
To install Maven on Linux, run the following command:
```
sudo apt-get install maven
```
To compile the project, run the following command from the root directory of the project:
```
mvn clean install
```
This will create a runnable jar called `scheduler.jar`.
To gain permissions to execute the jar, run the following command:
```
chmod 777 scheduler.jar
```

## Team Members
* Peter Lindsay - [ArtificialSnow](https://github.com/ArtificialSnow)
* Yuno Oh - [yuno99825](https://github.com/yuno99825)
* Sheldon Rodricks - [shelrod24](https://github.com/shelrod24)
* Tushar Thakur - [thakurtushar02](https://github.com/thakurtushar02)
* Elisa Yansun - [Milk-Yan](https://github.com/Milk-Yan)

## Acknowledgements
* [GraphStream](http://graphstream-project.org/)
* [Commons CLI](https://commons.apache.org/proper/commons-cli/)
* [JUnit 4](https://junit.org/junit4/)
* [JitPack](https://jitpack.io/)
* [Reducing the solution space of optimal task scheduling](http://www.sciencedirect.com/science/article/pii/S0305054813002542)
* [Optimal Task Scheduling on Parallel Systems](https://researchspace.auckland.ac.nz/handle/2292/27803)
