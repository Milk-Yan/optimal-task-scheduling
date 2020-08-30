package gui;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import solution.SolutionThread;

/**
 * Visualiser is run on the main thread and is responsible for loading the stage in which
 * the GUI is run
 */
public class Visualiser extends Application {

    private Controller controller;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader  = new FXMLLoader(getClass().getResource("visualisation-view.fxml"));
        Parent root = loader.load();

        this.controller = loader.getController();

        primaryStage.setTitle("Task Scheduler Visualization");
        primaryStage.setScene(new Scene(root, 810, 525));
        primaryStage.getIcons().add(new Image("gui/logo.png"));
        primaryStage.show();

        // When the GUI window is closed, the algorithm is also aborted.
        primaryStage.setOnCloseRequest(event -> System.exit(0));

    }

    /**
     * This method sets up required fields in the controller object which is then shown on the GUI.
     * @param solutionThread the thread in which the solution is run. Used to communicate with the GUI.
     */
    public void setUpArgs(SolutionThread solutionThread, int numProcessors, String fileName, int numTasks, int numThreads) {
        controller.setUpArgs(solutionThread, numProcessors, fileName, numTasks, numThreads);
    }
}
