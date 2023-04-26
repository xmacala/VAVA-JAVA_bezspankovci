package eu.fiit.cookingmanager.cookingmanager.controller;
import eu.fiit.cookingmanager.cookingmanager.repository.entity.User;
import eu.fiit.cookingmanager.cookingmanager.utils.DBUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.ResourceBundle;

public class AdminPanelController implements Initializable {

    @FXML public ScrollPane usersScroll;
    //@FXML public VBox usersList;
    @FXML private Button btn_logout;
    @FXML private Button btn_back;

    private final static Logger logger = LogManager.getLogger(AdminPanelController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        DBUtils dbUtils = new DBUtils();
        HashMap<String, User> users = new HashMap<>();
        VBox usersList = new VBox();

        try {
            Connection conn = dbUtils.dbConnect();

            String query = "SELECT * FROM public.user WHERE user_type_id < 3" ;

            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                User user = new User();

                user.setName(rs.getString("name").trim());
                user.setEmail(rs.getString("email").trim());
                user.setSurname(rs.getString("surname").trim());
                user.setUserTypeId(rs.getInt("user_type_id"));

                users.put(rs.getString("name").trim(), user);
            }

            for (String recipeKey : users.keySet()) {
                User user = users.get(recipeKey);

                Pane userPanel = new Pane();
                userPanel.setStyle("-fx-background-color: #fff; -fx-padding: 20px; -fx-border-radius: 15 15 15 15; -fx-border-color: #239c9c;");
                userPanel.minHeight(75);
                userPanel.minWidth(1155);
                userPanel.maxHeight(90);

                Text userName = new Text("Name: " + user.getName());
                userName.setStyle("-fx-font: normal bold 23px 'sans-serif'");
                userName.setX(20.0);
                userName.setY(40.0);

                Text userSurname = new Text("Surname: " + user.getSurname());
                userSurname.setStyle("-fx-font: normal 18px 'sans-serif'");
                userSurname.setX(20.0);
                userSurname.setY(60.0);

                Text userEmail = new Text("Email: " + user.getEmail());
                userEmail.setStyle("-fx-font: normal 18px 'sans-serif'");
                userEmail.setX(20.0);
                userEmail.setY(80.0);

                Text userType = new Text();
                if (user.getUserTypeId() == 1){
                    userType = new Text("Type: Guest");
                    userType.setStyle("-fx-font: normal 18px 'sans-serif'");
                    userType.setX(20.0);
                    userType.setY(100.0);
                }
                else{
                    userType = new Text("Type: Chef");
                    userType.setStyle("-fx-font: normal 18px 'sans-serif'");
                    userType.setX(20.0);
                    userType.setY(100.0);
                }

                Button btn_delete = new Button(resourceBundle.getString("delete"));
                btn_delete.setLayoutX(1045);
                btn_delete.setLayoutY(55);
                btn_delete.setStyle("-fx-background-color : #239c9c; -fx-background-radius : 15 15 15 15; -fx-text-fill: white;");
                btn_delete.setOnMouseEntered(e -> btn_delete.setStyle("-fx-background-color : #07f7f7; -fx-background-radius : 15 15 15 15; -fx-text-fill: black;"));
                btn_delete.setOnMouseExited(e -> btn_delete.setStyle("-fx-background-color : #239c9c; -fx-background-radius : 15 15 15 15; -fx-text-fill: white;"));

                btn_delete.setOnAction(new EventHandler<ActionEvent>() {
                    @Override
                    public void handle(ActionEvent actionEvent) {
                        //user,account(user_id), plan(account_id), recipe(account_id), ingredient_recipe(recipe_id)
                        logger.info(String.format("Admin requested to delete user %s %s", user.getName(), user.getSurname()));
                        try {
                            int user_id = 0;
                            int account_id = 0;
                            String query = "SELECT * FROM public.user WHERE name=(?)";
                            PreparedStatement pstmt = conn.prepareStatement(query);
                            pstmt.setString(1, user.getName().trim());
                            ResultSet rs = pstmt.executeQuery();

                            while (rs.next()) {
                                user_id = rs.getInt("id");
                            }

                            query = "SELECT * FROM public.account WHERE user_id=(?)";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setInt(1, user_id);
                            rs = pstmt.executeQuery();

                            while (rs.next()) {
                                account_id = rs.getInt("id");
                            }

                            query = "SELECT * FROM public.plan WHERE account_id=(?)";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setInt(1, account_id);
                            rs = pstmt.executeQuery();

                            while (rs.next()) {
                                query = "DELETE FROM public.plan WHERE id=(?)";
                                pstmt = conn.prepareStatement(query);
                                pstmt.setInt(1, rs.getInt("id"));
                                pstmt.executeUpdate();
                            }

                            query = "SELECT * FROM public.recipe WHERE account_id=(?)";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setInt(1, account_id);
                            rs = pstmt.executeQuery();

                            while (rs.next()) {
                                int recipe_id = rs.getInt("id");

                                query = "SELECT * FROM public.ingredient_recipe WHERE recipe_id=(?)";
                                pstmt = conn.prepareStatement(query);
                                pstmt.setInt(1, recipe_id);
                                rs = pstmt.executeQuery();

                                while (rs.next()) {
                                    query = "DELETE FROM public.ingredient_recipe WHERE id=(?)";
                                    pstmt = conn.prepareStatement(query);
                                    pstmt.setInt(1, rs.getInt("id"));
                                    pstmt.executeUpdate();
                                }

                                query = "DELETE FROM public.recipe WHERE id=(?)";
                                pstmt = conn.prepareStatement(query);
                                pstmt.setInt(1, recipe_id);
                                pstmt.executeUpdate();

                            }

                            query = "DELETE FROM public.account WHERE id=(?)";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setInt(1, account_id);
                            pstmt.executeUpdate();

                            query = "DELETE FROM public.user WHERE id=(?)";
                            pstmt = conn.prepareStatement(query);
                            pstmt.setInt(1, user_id);
                            pstmt.executeUpdate();

                            DBUtils.changeScene(actionEvent, "adminPanel.fxml", "Admin Panel", resourceBundle);

                        } catch (SQLException e) {
                            logger.error(e.getMessage());
                        }
                    }
                });

                userPanel.getChildren().addAll(userName, userSurname, userEmail, userType, btn_delete); // add component to the Pane component
                usersList.getChildren().add(userPanel); // add new item to the Vbox
            }
            usersList.minWidth(1160);
            usersList.setStyle("-fx-spacing: 5px; -fx-background-color : white;");
            usersScroll.setStyle("-fx-background-color : white;");
            usersScroll.setContent(usersList);
        }
        catch (SQLException | NullPointerException e) {
            logger.error(e.getMessage());
        }

        btn_logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User logged out of the application");
                DBUtils.changeScene(actionEvent, "login.fxml", resourceBundle.getString("login_title"),  resourceBundle);
            }
        });

        btn_back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User requeted to go back to the home screen");
                DBUtils.changeScene(actionEvent, "home.fxml", resourceBundle.getString("cooking_manager"), resourceBundle);
            }
        });

    }
}
