module org.example.sae_appli {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens org.example.sae_appli to javafx.fxml;
    exports org.example.sae_appli;
    exports main; // <- pour que javafx.graphics puisse y accéder
    opens controleur to javafx.fxml;
    opens vue to javafx.fxml;
}