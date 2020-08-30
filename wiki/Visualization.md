# Visualization
As part of milestone 2, a visualization of the scheduling algorithm was added.
This can be enabled by including the `-v` flag when running the program.

## Goals
The goals of the visualization were to provide a meaningful reflection of the algorithm,
as it exhaustively searches all possible schedules. We wanted to include both a visual 
representation of the current best schedule created, and some informative statistics 
about the execution of the algorithm.
## Solution
The Graphical User Interface (GUI) for the visualization is shown below:
![](images/gui.png)

The main chart displays the current best schedule.
This is updated live as the algorithm executes, along with the time elapsed, states 
searched, and the best finishing time found so far.

## Implementation
[JavaFX](https://openjfx.io/) was used along with 
[Scene Builder](https://gluonhq.com/products/scene-builder/) in order to easily 
integrate with the rest of the project and speed up development. To convert the 
project into a java application, we made the `Visualiser` class extend 
`javafx.application.Application`. If the `-v` flag is received, we launch the 
application. The `Controller` class encapsulates the logic of the GUI, while the 
visual components are described in `visualisation-view.fxml`.

### Multi-threading
The application runs on the main GUI thread, which is automatically managed by JavaFX 
to poll for events. To achieve this, we wrapped the solution in a `SolutionThread` class
which extends `Thread`. This is instead where the solution runs. The class acts as a bridge
between the solution and the GUI, and contains fields the GUI needs which are updated as the
solution runs. From the JavaFX application thread in `Controller`, the GUI periodically 
polls the `SolutionThread` to receive updated information from the solution.

## Issues
The algorithm runs very quickly with a few nodes (<14) and/or processors, so the live 
updates to the GUI can be hard to see.
