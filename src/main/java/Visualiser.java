import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

public class Visualiser extends Application {

    private static int _numProcessors;
    private static String _fileName;
    private static int _numTasks;
    private static int _numThreads;

    @Override
    public void start(Stage primaryStage) throws Exception {
        FXMLLoader loader  = new FXMLLoader(getClass().getResource("visualisation-view.fxml"));
        Parent root = loader.load();

        Controller controller = loader.getController();
        controller.setUpArgs(_numProcessors, _fileName, _numTasks, _numThreads);
        controller.addTask(0,100,10);
        controller.addTask(1,200,10);
        controller.removeLast();
        controller.addTask(2,100,100);
        controller.addTask(0,100,150);

        controller.incrementState();
        controller.incrementState();

        controller.setBestFinishTime(200);

        primaryStage.setTitle("Task Scheduler Visualisation");
        primaryStage.setScene(new Scene(root, 800, 525));
        primaryStage.setResizable(false);
        primaryStage.getIcons().add(new Image("logo.png"));
        primaryStage.show();

        controller.addTask(0,100,260);
        controller.addTask(1,100,100);
        controller.removeLast();
        controller.addTask(3,100,100);
        controller.addTask(1,1000,500);
        controller.addTask(1,130,1500);
        controller.setStatusFinished();
    }

    public static void run(String[] args, int numProcessors, String fileName, int numTasks, int numThreads) {
        _numProcessors = numProcessors;
        _fileName = fileName;
        _numTasks = numTasks;
        _numThreads = numThreads;
        launch(args);
    }
}
