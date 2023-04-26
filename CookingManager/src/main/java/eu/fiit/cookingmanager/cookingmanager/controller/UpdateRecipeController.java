package eu.fiit.cookingmanager.cookingmanager.controller;

import eu.fiit.cookingmanager.cookingmanager.repository.entity.Recipe;
import eu.fiit.cookingmanager.cookingmanager.utils.DBUtils;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.ResourceBundle;

public class UpdateRecipeController implements Initializable {
    private static int recipe_id;
    @FXML private Button btn_logout;
    @FXML private Button btn_back_home;
    @FXML private Button btn_adding;
    @FXML private Label lbl_xmlFile;
    @FXML private TextField inputName;
    @FXML private TextField inputTime;
    @FXML private TextArea inputSteps;
    @FXML private Button btn_save;
    @FXML private Button btn_delete;
    @FXML private VBox vbox_ingredients;
    @FXML private ChoiceBox choiceType;
    @FXML private Button btn_discard;
    String[] ingredientNameArray = {};
    Object[][] arr_ing = {};
    static Recipe recipe = null;
    String[] ingredientNameArrayClear = {};
    private final static Logger logger = LogManager.getLogger(AddRecipeController.class);



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {


        try{
            Connection conn = new DBUtils().dbConnect();
            String query = "Select * from public.ingredient";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                int ing_id = rs.getInt("id");
                String ing_name = rs.getString("name");
                String ing_uom = rs.getString("uom");

                int ing_weight = 0;
                String[] newArrayName = Arrays.copyOf(ingredientNameArray, ingredientNameArray.length + 1);
                String[] newArrayNameClear = Arrays.copyOf(ingredientNameArrayClear, ingredientNameArrayClear.length + 1);

                int k;
                for(k = 0; k < ingredientNameArray.length; k++) {
                    newArrayName[k] = ingredientNameArray[k];
                    newArrayNameClear[k] = ingredientNameArrayClear[k];
                }
                newArrayName[k] = ing_name + " [" + ing_uom + "]"  ;
                newArrayNameClear[k] = ing_name ;

                ingredientNameArray = newArrayName.clone();
                ingredientNameArrayClear = newArrayNameClear.clone();

                Object[][] arrNew = new Object[arr_ing.length + 1][3];

                for (int i = 0; i < arr_ing.length; i++) {
                    for (int j = 0; j < 3; j++) {
                        arrNew[i][j] = arr_ing[i][j];
                    }
                }

                arrNew[arrNew.length -1] = new Object[] {ing_id, ing_name, ing_uom,ing_weight};
                arr_ing = arrNew.clone();
            }
            DBUtils.dbDisconnect(conn);

        }
        catch (Exception e) {
            lbl_xmlFile.setText("Database disconnected");
            lbl_xmlFile.setTextFill(Color.color(1,0,0));
            e.printStackTrace();
        }

        String [] arr = {};
        try{
            Connection conn = new DBUtils().dbConnect();
            String query = "Select type from public.food_type";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();


            //robiš že hladáš types s DB
            while (rs.next()) {

                String food_type_element = rs.getString("type");

                String arrNew[] = new String[arr.length + 1];
                int i;
                for(i = 0; i < arr.length; i++) {
                    arrNew[i] = arr[i];
                }
                arrNew[i] = food_type_element;
                arr = arrNew.clone();
            }
            DBUtils.dbDisconnect(conn);

            //System.out.println(arr[1]);
            choiceType.setItems(FXCollections.observableArrayList(arr)
            );

        }
        catch (Exception e) {

            lbl_xmlFile.setText("Database disconnected");
            lbl_xmlFile.setTextFill(Color.color(1,0,0));
            e.printStackTrace();
        }

        try {
            Connection conn = new DBUtils().dbConnect();
            String query = "Select process from public.recipe WHERE id=(?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, recipe.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                inputSteps.setText(rs.getString("process"));
            }
            DBUtils.dbDisconnect(conn);

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        try {
            Connection conn = new DBUtils().dbConnect();
            String query = "SELECT i.name, i.uom, ir.pieces FROM ingredient i" +
                    " JOIN ingredient_recipe ir ON i.id=ir.ingredient_id" +
                    " WHERE ir.recipe_id=(?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, recipe.getId());
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()){
                addNewChoiceBox(rs.getString("name") + " [" + rs.getString("uom") + "]", rs.getString("pieces"));
            }

            DBUtils.dbDisconnect(conn);

        } catch (SQLException e) {
            logger.error(e);
        }

        vbox_ingredients.setSpacing(10);
        vbox_ingredients.setPadding(new Insets(10));
        inputName.setText(recipe.getName());
        inputTime.setText(String.valueOf(recipe.getTimeToCook()));
        choiceType.setValue(recipe.getFoodType());

        btn_logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User logged out of the application");
                DBUtils.changeScene(actionEvent, "login.fxml", resourceBundle.getString("login_title"), resourceBundle);
            }
        });

        btn_back_home.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User requested to go back to the home screen");
                DBUtils.changeScene(actionEvent, "home.fxml", resourceBundle.getString("cooking_manager"), resourceBundle);
            }
        });

        btn_discard.setOnAction(new EventHandler<ActionEvent>(){

            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User discarded recipe creation");
                DBUtils.changeScene(actionEvent, "home.fxml", "Cooking Manager", resourceBundle);
            }
        });

        btn_save.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    Connection conn = new DBUtils().dbConnect();
                    String query = "UPDATE public.recipe SET account_id=(?) WHERE id=(?)";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, GlobalVariableUser.getAccountId());
                    pstmt.setInt(2, recipe.getId());
                    pstmt.executeUpdate();

                    query = "UPDATE public.recipe SET \"name\"=(?) WHERE id=(?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, inputName.getText());
                    pstmt.setInt(2, recipe.getId());
                    pstmt.executeUpdate();

                    query = "UPDATE public.recipe SET time_to_cook=(?) WHERE id=(?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, Integer.parseInt(inputTime.getText()));
                    pstmt.setInt(2, recipe.getId());
                    pstmt.executeUpdate();

                    query = "UPDATE public.recipe SET process=(?) WHERE id=(?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, inputSteps.getText());
                    pstmt.setInt(2, recipe.getId());
                    pstmt.executeUpdate();


                    query = "SELECT * FROM public.food_type WHERE type=(?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setString(1, (String) choiceType.getValue());
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        int id = rs.getInt("id");
                        query = "UPDATE public.recipe SET food_type_id=(?) WHERE id=(?)";
                        pstmt = conn.prepareStatement(query);
                        pstmt.setInt(1, id);
                        pstmt.setInt(2, recipe.getId());
                        pstmt.executeUpdate();
                    }
                    DBUtils.changeScene(actionEvent, "home.fxml", "Cooking Manager", resourceBundle);

                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }


            }
        });
        btn_delete.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                //vymazat recipe ako admin
                //recipe, ingredient_recipe, plan
                logger.info(String.format("Administrator requested to delete recipe number %d", recipe_id));
                try {
                    Connection conn = new DBUtils().dbConnect();
                    String query = "SELECT * FROM public.plan WHERE recipe_id=(?)";
                    PreparedStatement pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, recipe_id);
                    ResultSet rs = pstmt.executeQuery();

                    while (rs.next()) {
                        query = "DELETE FROM public.plan WHERE recipe_id=(?)";
                        pstmt = conn.prepareStatement(query);
                        pstmt.setInt(1, recipe_id);
                        pstmt.executeUpdate();
                    }

                    query = "SELECT * FROM public.ingredient_recipe WHERE recipe_id=(?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, recipe_id);
                    rs = pstmt.executeQuery();

                    while (rs.next()) {
                        query = "DELETE FROM public.ingredient_recipe WHERE recipe_id=(?)";
                        pstmt = conn.prepareStatement(query);
                        pstmt.setInt(1, recipe_id);
                        pstmt.executeUpdate();
                    }

                    query = "DELETE FROM public.recipe WHERE id=(?)";
                    pstmt = conn.prepareStatement(query);
                    pstmt.setInt(1, recipe_id);
                    pstmt.executeUpdate();
                    DBUtils.changeScene(actionEvent, "home.fxml", resourceBundle.getString("cooking_manager"), resourceBundle);

                } catch (SQLException e) {
                    logger.error(e.getMessage());
                }

            }
        });

        btn_adding.setOnAction(event -> addNewChoiceBox());

    }

    public static void setRecipeDetails(Recipe rec){
        recipe = rec;
    }

    private void addNewChoiceBox() {

        HBox hbox = new HBox();
        hbox.setSpacing(10);

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(ingredientNameArray);

        //ScrollPane choicePanel = new ScrollPane(choiceBox);
        //choicePanel.setMaxHeight(1000);

        // Create a delete button for the new ChoiceBox
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            vbox_ingredients.getChildren().remove(hbox);
        });
        TextField textField = new TextField();
        textField.setPrefWidth(45);

        hbox.getChildren().addAll(choiceBox,textField, deleteButton);

        // Add the HBox to the VBox layout
        vbox_ingredients.getChildren().add(hbox);
    }

    private void addNewChoiceBox(String ing_name, String amount) {

        HBox hbox = new HBox();
        hbox.setSpacing(10);

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(ingredientNameArray);
        choiceBox.setValue(ing_name);

        //ScrollPane choicePanel = new ScrollPane(choiceBox);
        //choicePanel.setMaxHeight(1000);

        // Create a delete button for the new ChoiceBox
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            vbox_ingredients.getChildren().remove(hbox);
        });

        TextField textField = new TextField();
        textField.setText(amount);
        textField.setPrefWidth(45);


        hbox.getChildren().addAll(choiceBox,textField, deleteButton);

        // Add the HBox to the VBox layout
        vbox_ingredients.getChildren().add(hbox);
    }
    public static void setRecipe(int id){
        recipe_id = id;
    }

}
