package eu.fiit.cookingmanager.cookingmanager.controller;

import eu.fiit.cookingmanager.cookingmanager.utils.DBUtils;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.*;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class RecipeController implements Initializable {
    private static int recipe_id;
    @FXML TextField txt_recipeName;
    @FXML TextField txt_recipeType;
    @FXML TextField txt_recipeTime;
    @FXML TextArea txt_steps;
    @FXML Button btn_back;
    @FXML Button btn_logout;
    @FXML Button btn_createList;
    @FXML Button btn_save;
    @FXML VBox ingredients_v;

    private final static Logger logger = LogManager.getLogger(RecipeController.class);
    private List<List<String>> ingredients = new ArrayList<>();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        txt_steps.editableProperty().setValue(false);

        Connection conn = new DBUtils().dbConnect();
        int account_id = 0;

        try {
            String query = "SELECT * FROM public.recipe WHERE id=(?)";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, recipe_id);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                account_id = rs.getInt("account_id");
                txt_recipeName.setText(rs.getString("name").trim());
                txt_recipeTime.setText(String.valueOf(rs.getInt("time_to_cook")).trim());
                txt_steps.setText(rs.getString("process").trim());
                int food_type = rs.getInt("food_type_id");

                query = "SELECT * FROM public.food_type WHERE id=(?)";
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, food_type);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    txt_recipeType.setText(rs.getString("type").trim());
                }

                query = "SELECT i.name, i.uom, ir.pieces FROM ingredient i" +
                        " JOIN ingredient_recipe ir ON i.id=ir.ingredient_id" +
                        " WHERE ir.recipe_id = ?";

                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, recipe_id);
                rs = pstmt.executeQuery();

                while (rs.next()) {
                    ingredients.add(List.of(new String[] {rs.getString("name"), String.valueOf(rs.getInt("pieces")), rs.getString("uom")}));

                    addNewChoiceBox(rs.getString("name"),rs.getInt("pieces"), rs.getString("uom"));
                }

            }

            if (account_id == GlobalVariableUser.getAccountId() || GlobalVariableUser.getLogin().equals("admin")) {
                txt_recipeName.setEditable(true);
                txt_recipeTime.setEditable(true);
                txt_recipeType.setEditable(true);
                txt_steps.setEditable(true);
                btn_save.setVisible(true);
                txt_recipeName.setFont(Font.font("System", FontWeight.NORMAL, 12));
                txt_recipeTime.setFont(Font.font("System", FontWeight.NORMAL, 12));
                txt_recipeType.setFont(Font.font("System", FontWeight.NORMAL, 12));
                txt_steps.setFont(Font.font("System", FontWeight.NORMAL, 12));
            }
            else {
                txt_recipeName.setEditable(false);
                txt_recipeTime.setEditable(false);
                txt_recipeType.setEditable(false);
                txt_steps.setEditable(false);
                btn_save.setVisible(false);
                txt_recipeName.setFont(Font.font("System", FontWeight.BOLD, 12));
                txt_recipeTime.setFont(Font.font("System", FontWeight.BOLD, 12));
                txt_recipeType.setFont(Font.font("System", FontWeight.BOLD, 12));
                txt_steps.setFont(Font.font("System", FontWeight.BOLD, 12));
            }
            

        } catch (SQLException e) {
            logger.error(e.getMessage());
        }

        btn_logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User logged out of the application");
                DBUtils.changeScene(actionEvent, "login.fxml", resourceBundle.getString("login_title"), resourceBundle);
            }
        });

        btn_back.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User requested to go back to the home screen");
                DBUtils.changeScene(actionEvent, "home.fxml", resourceBundle.getString("cooking_manager"), resourceBundle);
            }
        });

        btn_createList.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.newDocument();

                    Element shoppingList = doc.createElement("shopping-list");
                    doc.appendChild(shoppingList);

                    for (List<String> i : ingredients) {
                        Element item = doc.createElement("list-item");

                        Element prod = doc.createElement("prod");
                        prod.appendChild(doc.createTextNode(i.get(0)));

                        Element pieces = doc.createElement("pieces");
                        pieces.appendChild(doc.createTextNode(i.get(1)));

                        Element unit = doc.createElement("unit");
                        unit.appendChild(doc.createTextNode(i.get(2)));

                        item.appendChild(prod);
                        item.appendChild(pieces);
                        item.appendChild(unit);

                        shoppingList.appendChild(item);
                    }


                    TransformerFactory tFactory = TransformerFactory.newInstance();
                    Transformer transformer = tFactory.newTransformer();

                    transformer.setOutputProperty(OutputKeys.INDENT, "yes");
                    transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");

                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File("cm-shopping-list.xml"));
                    transformer.transform(source, result);
                }
                catch(TransformerException | ParserConfigurationException e) {
                    logger.error(e.getMessage());
                }
            }
        });

    }

    public static void setRecipe(int id){
        recipe_id = id;
    }

    private void addNewChoiceBox(String name, int amount, String unit) {

        HBox hbox = new HBox();
        hbox.setMinWidth(347);
        hbox.setSpacing(10);
        hbox.setAlignment(Pos.CENTER);
        String strNumber = Integer.toString(amount);

        Label textfield1 = new Label();
        textfield1.setStyle("-fx-font: normal bold 18px 'sans-serif';");
        textfield1.setText(name);
        textfield1.setMinWidth(45);


        Label textField2 = new Label();
        textField2.setStyle("-fx-font: normal bold 18px 'sans-serif';");
        textField2.setText(strNumber);
        textField2.setMinWidth(45);

        Label textField3 = new Label();
        textField3.setStyle("-fx-font: normal bold 18px 'sans-serif';");
        textField3.setText(unit);
        textField3.setMinWidth(45);


        hbox.getChildren().addAll(textfield1,textField2, textField3);

        // Add the HBox to the VBox layout
        ingredients_v.getChildren().add(hbox);
    }
}
