package eu.fiit.cookingmanager.cookingmanager;

import eu.fiit.cookingmanager.cookingmanager.utils.DBUtils;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.Locale;
import java.util.ResourceBundle;


public class CookingManager extends Application {


    @Override
    public void start(Stage stage) throws IOException {
        Locale currentLocale = Locale.getDefault();
        ResourceBundle messages = ResourceBundle.getBundle("properties.messages", currentLocale);
        FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("login.fxml"), messages);

        Parent root = fxmlLoader.load();
        Scene scene = new Scene(root);
        stage.setTitle(messages.getString("login_title"));
        stage.setScene(scene);
        stage.setResizable(false);
        stage.show();

        new DBUtils().DBController();
    }

    public static void main(String[] args) {
        launch();
    }
}