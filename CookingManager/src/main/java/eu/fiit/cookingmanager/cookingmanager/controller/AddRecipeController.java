package eu.fiit.cookingmanager.cookingmanager.controller;



import eu.fiit.cookingmanager.cookingmanager.utils.DBUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import java.sql.SQLException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.*;
import java.util.List;

public class AddRecipeController implements Initializable {

    @FXML private Button btn_xmlFile;
    @FXML private Button btn_logout;
    @FXML private Button btn_back_home;
    @FXML private Button btn_adding;
    @FXML private Label lbl_xmlFile;
    @FXML private TextField inputName;
    @FXML private TextField inputTime;
    @FXML private TextArea inputSteps;
    @FXML private Button btn_exampleFile;
    @FXML private Button btn_recipe;
    @FXML private VBox vbox_ingredients;
    @FXML private ChoiceBox choiceType;
    @FXML private Button btn_discard;
    FileChooser fileChooser = new FileChooser();
    File selectedFile = null;
    String[] ingredientNameArray = {};

    Object[][] arr_ing = {};
    int type_id=0;

    int recept_id=0;
    String[] ingredientNameArrayClear = {};
    private final static Logger logger = LogManager.getLogger(AddRecipeController.class);

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {

        vbox_ingredients.setSpacing(10);
        vbox_ingredients.setPadding(new Insets(10));



        try{//Get information about all ingredients and fill into array
            Connection conn = new DBUtils().dbConnect();
            String query = "Select * from public.ingredient";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                //System.out.println("while");
                int ing_id = rs.getInt("id");
                String ing_name = rs.getString("name");
                String ing_uom = rs.getString("uom");

                int ing_weight = 0;
                //String ing_weight = rs.getString("type"); weight tam nieje v DB
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
            logger.error(e.getMessage());

        }

        String [] arr = {};
        try{//Get information about all food types and fill the panel
            Connection conn = new DBUtils().dbConnect();
            String query = "Select type from public.food_type";
            PreparedStatement pstmt = conn.prepareStatement(query);
            ResultSet rs = pstmt.executeQuery();
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

            choiceType.setItems(FXCollections.observableArrayList(arr));
            choiceType.setValue("");
        }
        catch (Exception e) {

            lbl_xmlFile.setText("Database disconnected");
            lbl_xmlFile.setTextFill(Color.color(1,0,0));
            logger.error(e.getMessage());
        }

        btn_xmlFile.setOnAction(new EventHandler<ActionEvent>(){
            @Override
            public void handle(ActionEvent actionEvent) {
                Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
                fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("All Files", "*.xml"));
                selectedFile = fileChooser.showOpenDialog(stage);
                if(selectedFile != null){

                    //System.out.println(selectedFile.getAbsolutePath());
                    clearPage();


                    try {
                        // Create a new DocumentBuilderFactory and DocumentBuilder
                        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                        DocumentBuilder builder = factory.newDocumentBuilder();
                        // Parse the XML file into a Document object
                        Document document = builder.parse(selectedFile.getAbsolutePath());
                        // Get the root element of the XML document
                        Element root = document.getDocumentElement();
                        // Get a list of all the "recept" elements
                        NodeList receptList = root.getElementsByTagName("recept");
                        if (receptList.getLength() == 1){
                            // Loop through each "recept" element and print its name, type, time, ingredients, and steps
                            for (int i = 0; i < receptList.getLength(); i++) {
                                org.w3c.dom.Node receptNode = receptList.item(i);
                                if (receptNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                    Element receptElement = (Element) receptNode;
                                    String name = receptElement.getElementsByTagName("name").item(0).getTextContent();
                                    String type = receptElement.getElementsByTagName("type").item(0).getTextContent();
                                    String time = receptElement.getElementsByTagName("time").item(0).getTextContent();
                                    NodeList ingredientList = receptElement.getElementsByTagName("ingredient");
                                    String steps = receptElement.getElementsByTagName("steps").item(0).getTextContent();

                                    for (int j = 0; j < ingredientList.getLength(); j++) {
                                        org.w3c.dom.Node ingredientNode = ingredientList.item(j);
                                        if (ingredientNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                            Element ingredientElement = (Element) ingredientNode;
                                            String ingName = ingredientElement.getElementsByTagName("ing_name").item(0).getTextContent();
                                            int ingAmount = Integer.parseInt(ingredientElement.getElementsByTagName("ing_amount").item(0).getTextContent());
                                            //System.out.println("- " + ingName + ": " + ingAmount);
                                        }
                                    }

                                    Connection conn = new DBUtils().dbConnect();
                                    String query = "Select * from public.food_type where type=(?)";
                                    PreparedStatement pstmt = conn.prepareStatement(query);
                                    pstmt.setString(1, type);
                                    ResultSet rs = pstmt.executeQuery();
                                    boolean is_food_type = rs.next();
                                    DBUtils.dbDisconnect(conn);

                                    if( is_food_type) {
                                        //System.out.println("Hello this food is here");
                                        inputName.setText(name);
                                        inputTime.setText(time);
                                        inputSteps.setText(steps);
                                        choiceType.setValue(type);
                                        lbl_xmlFile.setText(selectedFile.getName());
                                        lbl_xmlFile.setTextFill(Color.color(0, 0, 0));
                                        for (int l = 0; l < ingredientList.getLength(); l++)
                                        {
                                            //addNewChoiceBox();
                                            //System.out.println(ingredientList.toString());
                                            org.w3c.dom.Node ingredientNode = ingredientList.item(l);
                                            if (ingredientNode.getNodeType() == org.w3c.dom.Node.ELEMENT_NODE) {
                                                Element ingredientElement = (Element) ingredientNode;
                                                String ingNameFill = ingredientElement.getElementsByTagName("ing_name").item(0).getTextContent();
                                                int ingAmountFill = Integer.parseInt(ingredientElement.getElementsByTagName("ing_amount").item(0).getTextContent());

                                                int u;
                                                boolean found = false;
                                                for (u = 0; u < ingredientNameArrayClear.length; u++) {
                                                    if (ingredientNameArrayClear[u].equals(ingNameFill)) {
                                                        found = true;
                                                        break;
                                                    }
                                                }

                                                if(found == true) {

                                                    addNewChoiceBox(ingredientNameArray[u],ingAmountFill);
                                                    //addNewChoiceBoxWithScrollBar(ingredientNameArray[u],ingAmountFill);

                                                    //btn_adding.setOnAction(event -> addNewChoiceBoxWithScrollBar());
                                                }

                                                else
                                                {
                                                    clearPage();

                                                    lbl_xmlFile.setText("Invalid XML file");
                                                    lbl_xmlFile.setTextFill(Color.color(1,0,0));
                                                    break;
                                                }
                                            }
                                        }
                                    }
                                    else
                                    {
                                        lbl_xmlFile.setText("Invalid food type");
                                        lbl_xmlFile.setTextFill(Color.color(1,0,0));
                                    }

                                }
                            }

                        }
                        else {
                            lbl_xmlFile.setText("Invalid XML file");
                            lbl_xmlFile.setTextFill(Color.color(1,0,0));
                        }
                    } catch (Exception e) {
                        lbl_xmlFile.setText("Invalid XML file");
                        lbl_xmlFile.setTextFill(Color.color(1,0,0));
                        logger.error(e.getMessage());

                    }
                }
            }
        });



        btn_logout.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {
                logger.info("User logged out of the application");
                DBUtils.changeScene(actionEvent, "login.fxml", resourceBundle.getString("login_title"), resourceBundle);
            }
        });

        btn_exampleFile.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent actionEvent) {

                try {
                    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
                    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
                    Document doc = dBuilder.newDocument();

                    // Create the root element
                    Element rootElement = doc.createElement("recipe");
                    doc.appendChild(rootElement);

                    // Create the child elements
                    Element name = doc.createElement("name");
                    name.appendChild(doc.createTextNode("Write name of the recipe here"));
                    rootElement.appendChild(name);

                    Element type = doc.createElement("type");
                    type.appendChild(doc.createTextNode("Write type of food here"));
                    rootElement.appendChild(type);

                    Element time = doc.createElement("time");
                    time.appendChild(doc.createTextNode("Write time to prepare here"));
                    rootElement.appendChild(time);

                    Element ingredients = doc.createElement("ingredients");
                    rootElement.appendChild(ingredients);



                    Element ingredient = doc.createElement("ingredient");
                    Element ingredient_name = doc.createElement("ing_name");
                    Element ingredient_amount = doc.createElement("ing_amount");

                    ingredient_name.appendChild(doc.createTextNode("Write name of 1st ingredient here"));
                    ingredient_amount.appendChild(doc.createTextNode("Write amount of 1st ingredient here"));

                    ingredient.appendChild( ingredient_name);
                    ingredient.appendChild( ingredient_amount);

                    Element ingredient1 = doc.createElement("ingredient");
                    Element ingredient_name1 = doc.createElement("ing_name");
                    Element ingredient_amount1 = doc.createElement("ing_amount");

                    ingredient_name1.appendChild(doc.createTextNode("Write name of 2nd ingredient here"));
                    ingredient_amount1.appendChild(doc.createTextNode("Write amount of 2nd ingredient here"));

                    ingredient1.appendChild( ingredient_name1);
                    ingredient1.appendChild( ingredient_amount1);


                    Element ingredient2 = doc.createElement("ingredient");
                    Element ingredient_name2 = doc.createElement("ing_name");
                    Element ingredient_amount2 = doc.createElement("ing_amount");

                    ingredient2.appendChild(doc.createTextNode("You can append more ingredient after coppying this element"));
                    ingredient_name2.appendChild(doc.createTextNode(""));
                    ingredient_amount2.appendChild(doc.createTextNode(""));

                    ingredient2.appendChild( ingredient_name2);
                    ingredient2.appendChild( ingredient_amount2);




                    ingredients.appendChild(ingredient);
                    ingredients.appendChild(ingredient1);
                    ingredients.appendChild(ingredient2);

                    Element steps = doc.createElement("steps");
                    steps.appendChild(doc.createTextNode("Add some inportant info and process HERE"));
                    rootElement.appendChild(steps);

                    // Write the document to a file
                    TransformerFactory transformerFactory = TransformerFactory.newInstance();
                    Transformer transformer = transformerFactory.newTransformer();
                    DOMSource source = new DOMSource(doc);
                    StreamResult result = new StreamResult(new File("Example_file.xml"));
                    transformer.transform(source, result);

                    //System.out.println("Empty XML file created successfully.");

                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
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


        btn_recipe.setOnAction(new EventHandler<ActionEvent>(){

                                   @Override

                                   public void handle(ActionEvent actionEvent) {


                                       //System.out.print(inputName.getText());
                                       //System.out.print(inputTime.getText());
                                       //System.out.print(inputSteps.getText());
                                       //System.out.print(choiceType.getValue());

                                       //System.out.println(vbox_ingredients.getChildren().size());
                                       //System.out.println(vbox_ingredients.getChildren());
                                       if(!inputName.getText().equals("") && !inputTime.getText().equals("")  && !inputSteps.getText().equals("")  && !choiceType.getValue().equals(""))
                                       {

                                           //.out.println(vbox_ingredients.getChildren());


                                           if(vbox_ingredients.getChildren().size()>0)
                                           {
                                               //idem hladat či už taký recept v DB nieje
                                               try{
                                                   Connection conn = new DBUtils().dbConnect();

                                                   String query = "SELECT name FROM public.recipe WHERE name=(?)";
                                                   PreparedStatement pstmt = conn.prepareStatement(query);

                                                   pstmt.setString(1, inputName.getText());
                                                   ResultSet rs = pstmt.executeQuery();

                                                   if (rs.next())
                                                   {
                                                       //System.out.println("This name is in DB");
                                                       lbl_xmlFile.setText("Name already claimed");
                                                       lbl_xmlFile.setTextFill(Color.color(1,0,0));
                                                       DBUtils.dbDisconnect(conn);
                                                   }
                                                   else {
                                                       DBUtils.dbDisconnect(conn);
                                                       //ak je volne meno atm vieme že to má aspoň 1ingredienciu resp jeden hbox už treba naukladať ingrediencie do jedneho poľa aby sme sa vyhli duplikatom a push do DB
                                                       //System.out.println("Free nieje v DB");

                                                       ObservableList<Node> children = vbox_ingredients.getChildren();
                                                       List<List<Object>> myArray = new ArrayList<>();
                                                       int pom = 0;
                                                       for (Node child : children) {
                                                           int data = 0;
                                                           Object data2 = null;
                                                           if (child instanceof HBox) {
                                                               HBox hbox = (HBox) child;
                                                               ObservableList<Node> hboxChildren = hbox.getChildren();
                                                               for (Node hboxChild : hboxChildren) {
                                                                   if (hboxChild instanceof TextField) {
                                                                       TextField textField = (TextField) hboxChild;
                                                                       data = Integer.parseInt(textField.getText());

                                                                   } else if (hboxChild instanceof ChoiceBox<?>) {
                                                                       ChoiceBox choiceboxik = (ChoiceBox) hboxChild;
                                                                       data2 = choiceboxik.getValue();

                                                                   }
                                                               }
                                                           }


                                                           if (data > 0 && data2 != null) {

                                                               if (myArray.size() > 0) {
                                                                   for (int t = 0; t < myArray.size(); t++) {
                                                                       if (myArray.get(t).get(0) == data2) {
                                                                           int value = ((Integer) myArray.get(t).get(1)).intValue();
                                                                           myArray.get(t).set(1, value + data);
                                                                           break;
                                                                       } else if (t == myArray.size() - 1) {
                                                                           List<Object> row = new ArrayList<>();
                                                                           row.add(data2);
                                                                           row.add(data);
                                                                           row.add(0);
                                                                           myArray.add(row);
                                                                           break;
                                                                       }
                                                                   }
                                                               } else {
                                                                   List<Object> row = new ArrayList<>();
                                                                   row.add(data2);
                                                                   row.add(data);
                                                                   row.add(0);
                                                                   myArray.add(row);
                                                               }
                                                           }

                                                           //sem treba pridať exception


                                                           pom++;
                                                       }
                                                       //System.out.println("dlzka pola");
                                                       //System.out.println(myArray.size());

                                                       //treba zmazať z poľa jednotky

                                                       for (int q = 0; q < myArray.size(); q++) {
                                                           for (int r = 0; r < ingredientNameArray.length; r++) {
                                                               if (myArray.get(q).get(0).equals(ingredientNameArray[r])) {
                                                                   myArray.get(q).set(0, ingredientNameArrayClear[r]);
                                                                   for (int z = 0; z < arr_ing.length; z++) {
                                                                       if (arr_ing[z][1].equals(ingredientNameArrayClear[r])) {
                                                                           myArray.get(q).set(2, arr_ing[z][0]);
                                                                       }
                                                                   }
                                                               }
                                                           }
                                                       }


                                                       try {
                                                           conn = new DBUtils().dbConnect();
                                                           //System.out.println("this try");
                                                           // SELECT id FROM public.food_type WHERE type = 'Vegan'

                                                           query = "SELECT id FROM public.food_type WHERE type=(?)";
                                                           pstmt = conn.prepareStatement(query);
                                                           String selectedValue = String.valueOf(choiceType.getValue());
                                                           pstmt.setString(1, selectedValue);
                                                           rs = pstmt.executeQuery();
                                                           //System.out.println("testing here");
                                                           rs.next();
                                                           type_id = rs.getInt("id");
                                                           //System.out.println(type_id);
                                                           DBUtils.dbDisconnect(conn);
                                                           //System.out.println("this try");
                                                       } catch (Exception e) {
                                                           logger.error(e.getMessage());
                                                       }

                                                       String replacedString = inputSteps.getText().replace("\n", "\n");
                                                       //String testing = new String("Insert into public.recipe (account_id, name, food_type_id, time_to_cook, process) VALUES (" + GlobalVariableUser.getUserId() + ",'" + inputName.getText() + "'," + type_id +"," + inputTime.getText() + ", '{\"key\": \"" + replacedString + "\"}')");


                                                       String testing = new String("INSERT INTO public.recipe (account_id, name, food_type_id , time_to_cook, process) VALUES (?, ?, ?, ?, ?)");

                                                       if(myArray.size()>0){
                                                           try {

                                                               conn = new DBUtils().dbConnect();

                                                               pstmt = conn.prepareStatement(testing);
                                                               pstmt.setInt(1, GlobalVariableUser.getUserId());
                                                               pstmt.setString(2, inputName.getText());
                                                               pstmt.setInt(3, type_id);
                                                               pstmt.setInt(4, Integer.parseInt(inputTime.getText()));
                                                               pstmt.setString(5, replacedString);
                                                               pstmt.executeUpdate();
                                                               conn.setAutoCommit(false); // start a transaction

                                                               conn.commit(); // commit the transaction

                                                               //System.out.println("All queries executed successfully");

                                                               DBUtils.dbDisconnect(conn);


                                                           } catch (SQLException e) {
                                                               //System.out.println("Error executing queries: " + e.getMessage());
                                                               lbl_xmlFile.setText("Invalid Inputs");
                                                               lbl_xmlFile.setTextFill(Color.color(1, 0, 0));
                                                               try {
                                                                   conn.rollback(); // rollback the transaction if an error occurs
                                                                   //System.out.println("All changes rolled back successfully");
                                                               } catch (SQLException e1) {
                                                                   //System.out.println("Error rolling back changes: " + e1.getMessage());
                                                                   logger.error(e.getMessage());
                                                               }
                                                           }


                                                           try {
                                                               conn = new DBUtils().dbConnect();

                                                               String recipeIdQuery = new String("SELECT * FROM public.recipe WHERE name=(?)");
                                                               pstmt = conn.prepareStatement(recipeIdQuery);
                                                               pstmt.setString(1, inputName.getText().trim());
                                                               rs = pstmt.executeQuery();
                                                               rs.next();
                                                               recept_id = rs.getInt("id");
                                                               DBUtils.dbDisconnect(conn);
                                                               //System.out.println("Name exist in db");
                                                           } catch (Exception e) {

                                                               lbl_xmlFile.setText("Invalid Inputs");
                                                               lbl_xmlFile.setTextFill(Color.color(1, 0, 0));
                                                               logger.error(e.getMessage());

                                                           }
                                                           //sem treba ingrediencie narobiť Q v loope
                                                           String[] queries = new String[]{};
                                                           //v myArray mám atm uložene System.out.println(myArray.get(au).get(0)); index 0 ingrediencia,1množstvo,2je ID


//        String query = "INSERT INTO my_table (recipe_id, ingredient_id, ingredient_value) VALUES (?, ?, ?)";

                                                           try {

                                                               String recipe_testing = new String("INSERT INTO public.ingredient_recipe (recipe_id, ingredient_id, pieces) VALUES (?, ?, ?)");
                                                               //v myArray mám atm uložene System.out.println(myArray.get(au).get(0)); index 0 ingrediencia,1množstvo,2je ID
                                                               conn = new DBUtils().dbConnect();
                                                               pstmt = conn.prepareStatement(recipe_testing);

                                                               for (int w = 0; w < myArray.size(); w++) {
                                                                   pstmt.setInt(1, recept_id);
                                                                   pstmt.setInt(2, (int) myArray.get(w).get(2));
                                                                   pstmt.setInt(3, (int) myArray.get(w).get(1));
                                                                   pstmt.addBatch();
                                                               }

                                                               pstmt.executeBatch();
                                                               int[] updateCounts = pstmt.executeBatch();

                                                               DBUtils.changeScene(actionEvent, "home.fxml", "Cooking Manager", resourceBundle);
                                                           } catch (SQLException e) {
                                                               //System.out.println("Error executing queries: " + e.getMessage());

                                                               lbl_xmlFile.setText("Invalid Inputs");
                                                               lbl_xmlFile.setTextFill(Color.color(1, 0, 0));
                                                               logger.error(e.getMessage());
                                                           }

                                                       }
                                                       else{
                                                           lbl_xmlFile.setText("Bad ingredients");
                                                           lbl_xmlFile.setTextFill(Color.color(1,0,0));
                                                           logger.error("Bad ingredients");
                                                       }

                                                   }
                                               }
                                               catch (Exception e) {
                                                   lbl_xmlFile.setText("Invalid Inputs");
                                                   lbl_xmlFile.setTextFill(Color.color(1,0,0));
                                                   logger.error(e.getMessage());
                                               }
                                           }
                                           else
                                           {
                                               lbl_xmlFile.setText("Missing Ingredients");
                                               lbl_xmlFile.setTextFill(Color.color(1,0,0));
                                               logger.error("Missing ingredients");
                                           }
                                       }
                                       else
                                       {
                                           //String testing = new String("Insert into public.recipe (account_id, name, food_type_id, time_to_cook, process) VALUES (" + GlobalVariableUser.getUserId() + "," + inputName + "," + choiceType +"," + inputTime + "'{\"key\": \"" + inputSteps + "\"}');\n");

                                           lbl_xmlFile.setText("Missing data");
                                           lbl_xmlFile.setTextFill(Color.color(1,0,0));
                                       }
                                   }
                               }
        );
        btn_adding.setOnAction(event -> addNewChoiceBox());
        //btn_adding.setOnAction(event -> addNewChoiceBoxWithScrollBar());

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


    private void addNewChoiceBox(String name, int amount) {


        HBox hbox = new HBox();
        hbox.setSpacing(10);
        String strNumber = Integer.toString(amount);

        ChoiceBox<String> choiceBox = new ChoiceBox<>();
        choiceBox.getItems().addAll(ingredientNameArray);
        choiceBox.setValue(name);

        //ScrollPane choicePanel = new ScrollPane(choiceBox);
        //choicePanel.setMaxHeight(1000);

        // Create a delete button for the new ChoiceBox
        Button deleteButton = new Button("Delete");
        deleteButton.setOnAction(event -> {
            vbox_ingredients.getChildren().remove(hbox);
        });

        TextField textField = new TextField();
        textField.setText(strNumber);
        textField.setPrefWidth(45);


        hbox.getChildren().addAll(choiceBox,textField, deleteButton);

        // Add the HBox to the VBox layout
        vbox_ingredients.getChildren().add(hbox);
    }

    private void clearPage(){
        inputName.setText("");
        inputTime.setText("");
        inputSteps.setText("");
        choiceType.setValue("");
        vbox_ingredients.getChildren().clear();

    }

}
