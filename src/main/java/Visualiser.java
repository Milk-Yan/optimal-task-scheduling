import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Visualiser extends Application {

    public void run(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        System.out.println("here");
        FXMLLoader loader  = new FXMLLoader(getClass().getResource("visualisation-view.fxml"));
        Parent root = loader.load();
        primaryStage.setTitle("Task Scheduler Visualisation");
        primaryStage.setScene(new Scene(root, 300, 275));
        primaryStage.show();

    }
}
