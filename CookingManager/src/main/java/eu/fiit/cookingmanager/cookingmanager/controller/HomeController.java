package eu.fiit.cookingmanager.cookingmanager.controller;

import eu.fiit.cookingmanager.cookingmanager.repository.entity.Recipe;
import eu.fiit.cookingmanager.cookingmanager.utils.DBUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class HomeController implements Initializable {

    @FXML public Button btn_adminPanel;
    @FXML private Button btn_loggout;
    @FXML private Button btn_addRecipe;
    @FXML private Label lbl_name;
    @FXML private VBox recipeList;
    @FXML private ScrollPane recipeScroll;
    @FXML private TextField searchTextField;
    @FXML private Button searchButton;
    @FXML
    private ComboBox<String> filterByType;

    private final static Logger logger = LogManager.getLogger(HomeController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        new RecipeController();
        recipeScroll.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        recipeScroll.setStyle("-fx-background-color: transparent");
        lbl_name.setText(GlobalVariableUser.getName());

        searchButton.setStyle("-fx-background-color : #239c9c; -fx-background-radius : 15 15 15 15; -fx-text-fill: white;");
        searchButton.setOnMouseEntered(e -> searchButton.setStyle("-fx-background-color : #07f7f7; -fx-background-radius : 15 15 15 15; -fx-text-fill: black;"));
        searchButton.setOnMouseExited(e -> searchButton.setStyle("-fx-background-color : #239c9c; -fx-background-radius : 15 15 15 15; -fx-text-fill: white;"));

        if (GlobalVariableUser.getType() == 1){
            btn_addRecipe.setVisible(false);
            btn_adminPanel.setVisible(false);
        } else if (GlobalVariableUser.getType() == 2) {
            btn_adminPanel.setVisible(false);
        }

        loadRecipes(resourceBundle);
        fetchRecipeTypes();

        btn_loggout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User logged out of the application");
                DBUtils.changeScene(actionEvent, "login.fxml", resourceBundle.getString("login_title"),  resourceBundle);
            }
        });

        btn_addRecipe.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User requested to create a new recipe");
                DBUtils.changeScene(actionEvent, "addRecipe.fxml", resourceBundle.getString("add_recipe_title"), resourceBundle);
            }
        });

        btn_adminPanel.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                DBUtils.changeScene(actionEvent, "adminPanel.fxml", "Admin Panel", resourceBundle);
            }
        });

        searchButton.setOnAction(event -> {
            String searchText = searchTextField.getText().trim();
            String selectedType = filterByType.getSelectionModel().getSelectedItem();
            filterRecipes(resourceBundle, searchText, selectedType);
        });
    }

    private void filterRecipes(ResourceBundle resourceBundle, String searchText, String recipeType) {
        DBUtils dbUtils = new DBUtils();
        try {
            Connection conn = dbUtils.dbConnect();
            ResultSet rs = loadQueryResult(conn, "SELECT r.id, r.name, r.account_id, r.time_to_cook, ft.type FROM recipe r" +
                    " JOIN food_type ft ON ft.id = r.food_type_id");

            List<Recipe> allRecipes = new ArrayList<>();
            while (rs.next()) {
                Recipe recipe = new Recipe();
                recipe.setId(rs.getInt("id"));
                recipe.setName(rs.getString("name"));
                recipe.setTimeToCook(rs.getInt("time_to_cook"));
                recipe.setFoodType(rs.getString("type"));

                allRecipes.add(recipe);
            }

            recipeList.getChildren().clear(); // Clear the VBox to display filtered recipes

            Pattern pattern = Pattern.compile(searchText, Pattern.CASE_INSENSITIVE);

            for (Recipe recipe : allRecipes) {
                Matcher matcher = pattern.matcher(recipe.getName());
                boolean nameMatches = matcher.find();
                boolean typeMatches = (recipeType == null || recipeType.isEmpty()) || recipeType.equals(recipe.getFoodType());

                if (nameMatches && typeMatches) {
                    // get author of the recipe
                    String accQuery = "SELECT u.\"name\", u.surname FROM account a" +
                            " JOIN \"user\" u ON u.id=a.user_id" +
                            " WHERE a.id=?";

                    PreparedStatement pstmtAuthor = conn.prepareStatement(accQuery);
                    pstmtAuthor.setInt(1, recipe.getAccountId());
                    ResultSet rsAuthor = pstmtAuthor.executeQuery();

                    buildRecipeListPanel(recipe, resourceBundle, rs, rsAuthor);
                }
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void loadRecipes(ResourceBundle resourceBundle) {
        DBUtils dbUtils = new DBUtils();
        HashMap<String, Recipe> recipes = new HashMap<>();

        try {
            Connection conn = dbUtils.dbConnect();
            ResultSet rs = loadQueryResult(conn, "SELECT r.id, r.name, r.account_id, r.time_to_cook, ft.type FROM recipe r" +
                            " JOIN food_type ft ON ft.id = r.food_type_id");

            // load data to hash-map for quicker search time
            while (rs.next()) {
                Recipe recipe = new Recipe();
                    recipe.setId(rs.getInt("id"));
                    recipe.setName(rs.getString("name"));
                    recipe.setTimeToCook(rs.getInt("time_to_cook"));
                    recipe.setFoodType(rs.getString("type"));
                    recipe.setAccountId(rs.getInt("account_id"));

                recipes.put(rs.getString("name"), recipe);
            }

            rs.close();

            // print recipes
            for (String recipeKey : recipes.keySet()) {
                Recipe recipe = recipes.get(recipeKey);

                // get author of the recipe
                String accQuery = "SELECT u.\"name\", u.surname FROM account a" +
                        " JOIN \"user\" u ON u.id=a.user_id" +
                        " WHERE a.id=(?)";

                PreparedStatement pstmtAuthor = conn.prepareStatement(accQuery);
                pstmtAuthor.setInt(1, recipe.getAccountId());
                ResultSet rsAuthor = pstmtAuthor.executeQuery();

                // prepare presentation
                buildRecipeListPanel(recipe, resourceBundle, rs, rsAuthor);
                Pane recipePanel = new Pane();
                recipePanel.setStyle("-fx-background-color: #fff; -fx-padding: 20px; -fx-cursor: hand; -fx-border-radius: 15 15 15 15; -fx-border-color: #239c9c ");

                // onClick event handler for recipes (opens recipe detail)
                recipePanel.setOnMouseClicked(e -> {
                    RecipeController.setRecipe(recipe.getId());
                    DBUtils.changeScene(e, "recipe.fxml", resourceBundle.getString("cooking_manager"), resourceBundle);
                });

                  Text recipeName = new Text(recipe.getName());
                      recipeName.setStyle("-fx-font: normal bold 23px 'sans-serif'");
                      recipeName.setX(20.0);
                      recipeName.setY(40.0);

                  Text recipeType = new Text("Food type: " + recipe.getFoodType());
                      recipeType.setStyle("-fx-font: normal 18px 'sans-serif'");
                      recipeType.setX(20.0);
                      recipeType.setY(70.0);

                  Text timeToCook = new Text("Time to cook: " + recipe.getTimeToCook() + " min.");
                      timeToCook.setStyle("-fx-font: normal 18px 'sans-serif'");
                      timeToCook.setX(20.0);
                      timeToCook.setY(100.0);

                    Text author = new Text();
                    if (rsAuthor.next()) {
                        author.setText("Author: " + rsAuthor.getString("name") + " " + rsAuthor.getString("surname"));
                    }
                    else {
                        author.setText("Author: Unknown");
                    }

                    author.setStyle("-fx-font: normal 18px 'sans-serif'");
                    author.setX(750.0);
                    author.setY(40.0);

                    recipePanel.getChildren().addAll(recipeName, recipeType, timeToCook, author); // add component to the Pane component

                    recipeList.getChildren().add(recipePanel); // add new item to the Vbox
                    recipeList.setStyle("-fx-background-color : white;");
          }
        }
        catch (SQLException | NullPointerException e) {
            logger.error(e.getMessage());
        }

    }

    private void buildRecipeListPanel(Recipe recipe, ResourceBundle resourceBundle, ResultSet rs, ResultSet rsAuthor) throws SQLException {
        Pane recipePanel = new Pane();
        recipePanel.setStyle("-fx-background-color: #fff; -fx-padding: 20px; -fx-cursor: hand; -fx-border-radius: 15 15 15 15; -fx-border-color: #239c9c ");
        DBUtils dbUtils = new DBUtils();

        // onClick event handler for recipes (opens recipe detail)
        recipePanel.setOnMouseClicked(e -> {

            int account_id = 0;
            try {
                Connection conn = dbUtils.dbConnect();
                String q = "SELECT * FROM public.recipe WHERE id=(?)";
                PreparedStatement p = conn.prepareStatement(q);
                p.setInt(1, recipe.getId());
                ResultSet r = p.executeQuery();

                while (r.next()) {
                    account_id = r.getInt("account_id");
                }
                System.out.println(account_id);
                if (account_id == GlobalVariableUser.getAccountId() || GlobalVariableUser.getLogin().equals("admin")) {
                    UpdateRecipeController.setRecipe(recipe.getId());
                    UpdateRecipeController.setRecipeDetails(recipe);
                    DBUtils.changeScene(e, "updateRecipe.fxml", resourceBundle.getString("add_recipe_title"), resourceBundle);
                }
                else {
                    RecipeController.setRecipe(recipe.getId());
                    DBUtils.changeScene(e, "recipe.fxml", resourceBundle.getString("cooking_manager"), resourceBundle);
                }
            } catch (SQLException ee) {
                logger.error(ee.getMessage());
            }
        });

        Text recipeName = new Text(recipe.getName());
        recipeName.setStyle("-fx-font: normal bold 23px 'sans-serif'");
        recipeName.setX(20.0);
        recipeName.setY(40.0);

        Text recipeType = new Text("Food type: " + recipe.getFoodType());
        recipeType.setStyle("-fx-font: normal 18px 'sans-serif'");
        recipeType.setX(20.0);
        recipeType.setY(70.0);

        Text timeToCook = new Text("Time to cook: " + recipe.getTimeToCook() + " min.");
        timeToCook.setStyle("-fx-font: normal 18px 'sans-serif'");
        timeToCook.setX(20.0);
        timeToCook.setY(100.0);

        if (rsAuthor != null) {
            Text author = new Text();
            if (rsAuthor.next()) {
                author.setText("Author: " + rsAuthor.getString("name") + " " + rsAuthor.getString("surname"));
            } else {
                author.setText("Author: Unknown");
            }
            author.setStyle("-fx-font: normal 18px 'sans-serif'");
            author.setX(750.0);
            author.setY(40.0);

            recipePanel.getChildren().addAll(recipeName, recipeType, timeToCook, author); // add component to the Pane component
        } else {
            recipePanel.getChildren().addAll(recipeName, recipeType, timeToCook);
        }

        recipeList.getChildren().add(recipePanel); // add new item to the Vbox
        recipeList.setStyle("-fx-background-color : white;");
    }

    public void fetchRecipeTypes() {
        DBUtils dbUtils = new DBUtils();
        try {
            Connection conn = dbUtils.dbConnect();
            ResultSet rs = loadQueryResult(conn, "SELECT DISTINCT type FROM food_type");

            List<String> recipeTypes = new ArrayList<>();
            while (rs.next()) {
                String type = rs.getString("type");
                recipeTypes.add(type);
            }

            // set items for the ComboBox
            filterByType.setItems(FXCollections.observableArrayList(recipeTypes));
            filterByType.getItems().add(null);

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private ResultSet loadQueryResult(Connection conn, String query){
        try {
            PreparedStatement pstmt = conn.prepareStatement(query);
            return pstmt.executeQuery();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

}
