module eu.fiit.cookingmanager.cookingmanager {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.sql;
    requires java.dotenv;
    requires json;
    requires log4j.api;

    opens eu.fiit.cookingmanager.cookingmanager to javafx.fxml;
    exports eu.fiit.cookingmanager.cookingmanager;
    exports eu.fiit.cookingmanager.cookingmanager.controller;
    opens eu.fiit.cookingmanager.cookingmanager.controller to javafx.fxml;
}