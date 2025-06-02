package vue;

import controleur.ControleurAccueil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import modele.ScenarioUtils;
import modele.Vente;

import javafx.geometry.Insets;
import javafx.geometry.Pos;

public class VueAccueil {

    private ComboBox<String> comboScenarios;
    private ListView<String> listeVentes;
    private ListView<String> listeItineraire;
    private Label labelDistance;
    private Label labelDistanceHeuristique;
    private TextField fieldK;
    private final ControleurAccueil controleur = new ControleurAccueil();

    public void start(Stage stage) {
        Label titre = new Label("Bienvenue dans l'application APPLI");
        titre.setStyle("-fx-font-size: 16px; -fx-font-weight: bold;");

        // === SECTION SÉLECTION SCÉNARIO ===
        comboScenarios = new ComboBox<>();
        comboScenarios.setPromptText("Choisir un scénario");
        comboScenarios.setItems(FXCollections.observableArrayList(ScenarioUtils.listerScenarios()));

        Button boutonCharger = new Button("Charger ce scénario");
        boutonCharger.setOnAction(e -> controleur.chargerScenario(
                comboScenarios, listeVentes, listeItineraire, labelDistance, labelDistanceHeuristique));

        Button ouvrirEditeur = new Button("Créer / Modifier un scénario");
        ouvrirEditeur.setOnAction(e -> new VueEditeurScenario().start(new Stage()));

        Button boutonRafraichir = new Button("Rafraîchir la liste");
        boutonRafraichir.setOnAction(e ->
                comboScenarios.setItems(FXCollections.observableArrayList(ScenarioUtils.listerScenarios()))
        );

        // === SECTION RÉSULTATS DES ALGORITHMES ===
        labelDistance = new Label("Distance totale : -");
        labelDistanceHeuristique = new Label("Distance heuristique : -");

        // === NOUVELLE SECTION : K MEILLEURES SOLUTIONS ===
        Label labelK = new Label("Nombre de solutions (k) :");
        fieldK = new TextField("5");
        fieldK.setPrefWidth(60);

        Button btnKMeilleures = new Button("Calculer k meilleures solutions");
        btnKMeilleures.setOnAction(e -> {
            try {
                int k = Integer.parseInt(fieldK.getText());
                if (k <= 0) {
                    showAlert("Erreur", "Veuillez entrer un nombre positif pour k");
                    return;
                }
                controleur.calculerKMeilleuresSolutions(comboScenarios, listeItineraire, k);
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Veuillez entrer un nombre valide pour k");
            }
        });

        Button btnComparerTout = new Button("Comparer tous les algorithmes");
        btnComparerTout.setOnAction(e -> {
            try {
                int k = Integer.parseInt(fieldK.getText());
                if (k <= 0) {
                    showAlert("Erreur", "Veuillez entrer un nombre positif pour k");
                    return;
                }
                controleur.calculerToutesLesSolutions(comboScenarios, listeItineraire, k);
            } catch (NumberFormatException ex) {
                showAlert("Erreur", "Veuillez entrer un nombre valide pour k");
            }
        });

        // Organiser les contrôles K en ligne horizontale
        HBox boxK = new HBox(10, labelK, fieldK, btnKMeilleures);
        boxK.setAlignment(Pos.CENTER);

        // === SECTION AFFICHAGE DES DONNÉES ===
        Label labelVentes = new Label("Ventes du scénario :");
        labelVentes.setStyle("-fx-font-weight: bold;");

        listeVentes = new ListView<>();
        listeVentes.setPrefHeight(120);

        Label labelItineraire = new Label("Résultats des algorithmes :");
        labelItineraire.setStyle("-fx-font-weight: bold;");

        listeItineraire = new ListView<>();
        listeItineraire.setPrefHeight(200);

        // === SÉPARATEURS VISUELS ===
        Separator sep1 = new Separator();
        Separator sep2 = new Separator();
        Separator sep3 = new Separator();

        // === ORGANISATION FINALE ===
        VBox layout = new VBox(10);
        layout.getChildren().addAll(
                titre,
                sep1,
                // Section chargement
                comboScenarios,
                boutonCharger,
                ouvrirEditeur,
                boutonRafraichir,
                sep2,
                // Section résultats simples
                labelDistance,
                labelDistanceHeuristique,
                // Section algorithmes avancés
                boxK,
                btnComparerTout,
                sep3,
                // Section affichage des données
                labelVentes,
                listeVentes,
                labelItineraire,
                listeItineraire
        );

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 900, 800);
        stage.setTitle("APPLI - Livraison de cartes Pokémon");
        stage.setScene(scene);
        stage.show();
    }

    /**
     * Méthode utilitaire pour afficher des alertes
     */
    private void showAlert(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}