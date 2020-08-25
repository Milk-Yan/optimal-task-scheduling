import javafx.application.Application;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.StackedBarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class Visualiser extends Application {

    public void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("here");
        FXMLLoader loader  = new FXMLLoader(getClass().getResource("visualisation-view.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        controller.setUpArgs(4);
        controller.addTask(0,100,10);
        controller.addTask(1,200,10);
        controller.removeLast();
        controller.addTask(2,100,100);
        controller.addTask(0,100,150);

        primaryStage.setTitle("Task Scheduler Visualisation");
        primaryStage.setScene(new Scene(root, 780, 475));
        primaryStage.setResizable(false);
        primaryStage.show();
    }
}
