package eu.fiit.cookingmanager.cookingmanager.utils;

import eu.fiit.cookingmanager.cookingmanager.CookingManager;
import eu.fiit.cookingmanager.cookingmanager.controller.HomeController;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Objects;
import java.util.ResourceBundle;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;



public class DBUtils {
    private Connection conn = null;
    private Dotenv env = null;
    private String connURL = null;

    private static final Logger logger = LogManager.getLogger(DBUtils.class);


    public void DBController() {
        this.env = Dotenv.load();
        this.connURL = String.format("jdbc:postgresql://%s/%s", this.env.get("DB_HOST"), this.env.get("DB_NAME"));
    }


    public Connection dbConnect() {
        DBController();
        try {
            String driver = "org.postgresql.Driver";
            Class.forName(driver);
        }
        catch(ClassNotFoundException classNotFoundException) {
            logger.error(classNotFoundException.getMessage());
        }

        try {
            this.conn = DriverManager.getConnection(this.connURL, this.env.get("DB_USER"), this.env.get("DB_PASS"));

        }
        catch (SQLException sqlException) {
            logger.error(sqlException.getMessage());
        }

        return this.conn;
    }

    public static void dbDisconnect(Connection conn) {
        try {
            if (conn != null && !conn.isClosed()) {
                conn.close();
            }
        }
        catch(SQLException sqlException) {
            logger.error(sqlException.getMessage());

        }

    }


    public static void changeScene(Event event, String fxmlFile, String title, ResourceBundle resourceBundle) {
        Parent root = null;
        try{
            root = FXMLLoader.load(CookingManager.class.getResource(fxmlFile), resourceBundle);
            Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            stage.setTitle(title);

            assert root != null;
            stage.setScene(new Scene(root));
            stage.show();
        } catch (IOException | NullPointerException e) {
            logger.error(e.getMessage());
        }
    }

}





