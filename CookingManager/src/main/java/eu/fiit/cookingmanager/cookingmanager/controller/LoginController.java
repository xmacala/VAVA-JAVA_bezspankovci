package eu.fiit.cookingmanager.cookingmanager.controller;

import eu.fiit.cookingmanager.cookingmanager.utils.DBUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ResourceBundle;


public class LoginController implements Initializable {

    @FXML private TextField tf_username;
    @FXML private PasswordField pf_password;
    @FXML private Button btn_signup;
    @FXML private Button btn_login;
    @FXML private Label lbl_error;

    private final static Logger logger = LogManager.getLogger(LoginController.class);


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        btn_login.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info(String.format("User requested to log into account %s", tf_username.getText().trim()));

                boolean isAuthenticated = false;

                try {
                    Connection conn = new DBUtils().dbConnect();

                    String query = "SELECT * FROM public.account WHERE login=(?)";
                    PreparedStatement pstmt = conn.prepareStatement(query);

                    pstmt.setString(1, tf_username.getText().trim());
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        int user_id = rs.getInt("user_id");
                        String password = rs.getString("password");
                        if (password.equals(pf_password.getText())) {
                            isAuthenticated = true;
                            GlobalVariableUser.setUser(user_id, conn);
                            DBUtils.dbDisconnect(conn);
                            break;
                        }
                    }

                } catch (SQLException | NullPointerException e) {
                    logger.error(e.getMessage());
                }

                if (isAuthenticated) {
                    logger.info(String.format("User %s successfully logged in", GlobalVariableUser.getName()));
                    DBUtils.changeScene(actionEvent, "home.fxml", resourceBundle.getString("cooking_manager"), resourceBundle);
                } else {
                    lbl_error.setText(resourceBundle.getString("credentials_mismatch"));
                    logger.error("Incorrect credentials");
                }
            }
        });

        btn_signup.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User requested to register a new account");
                DBUtils.changeScene(actionEvent, "register.fxml", resourceBundle.getString("sign_up_title"), resourceBundle);
            }
        });

    }
}
