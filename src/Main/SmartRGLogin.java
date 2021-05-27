package Main;

import ViewController.RunUnitController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class SmartRGLogin extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception{
        System.setProperty("webdriver.chrome.driver", "ChromeDriver/chromedriver");
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/ViewController/RunUnit.fxml"));
        Parent root = loader.load();
        RunUnitController controller = loader.getController();
        primaryStage.setOnCloseRequest(controller::onClose);
        primaryStage.setTitle("SmartRG Login App v1.1.3");
        primaryStage.setScene(new Scene(root, 400, 400));
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
