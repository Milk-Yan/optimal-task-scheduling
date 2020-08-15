# SOFTENG 306 Project 1 - Group 13 Notice me Sinnenpai
## Background
This project attempts to solve a difficult scheduling problem for the leader of a 'Big-As Parallel Computing Centre', who needs to schedule processes on their parallel computer systems. The objectives of the project are fast execution time and high quality software. More details and documentation about the project can be found in the [wiki](wiki/Home.md).

## Running the Project
There are two milestones for the project. There will be a GitHub release made for each milestone, including a runnable jar of the project.
To run the jar, use the following command:
```
java -jar schedular.jar INPUT.dot P [OPTION]
```
* `INPUT.dot` a task graph with integer weights in dot format
* `P` number of processors to schedule the INPUT graph on

OPTIONAL:
* `-p N` use `N` cores for execution in parallel (default is sequential)
* `-v` visualise the search
* `-o OUTPUT` output file is named `OUTPUT` (default is INPUT-output.dot)

### Compilation
This is a [Maven](https://maven.apache.org/) Project. To install Maven on Linux, run the following command.
```
sudo apt-get install mvn
```
To compile the project, run the following command from the root directory of the project:
```
mvn clean install
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
