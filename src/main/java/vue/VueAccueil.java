package vue;

import controleur.ControleurAccueil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    private final ControleurAccueil controleur = new ControleurAccueil();

    public void start(Stage stage) {
        Label titre = new Label("Bienvenue dans l'application APPLI");

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

        labelDistance = new Label("Distance totale : -");
        labelDistanceHeuristique = new Label("Distance heuristique : -");

        listeVentes = new ListView<>();
        listeVentes.setPrefHeight(150);

        listeItineraire = new ListView<>();
        listeItineraire.setPrefHeight(150);

        VBox layout = new VBox(10, titre, comboScenarios, boutonCharger, ouvrirEditeur, boutonRafraichir,
                labelDistance, listeVentes,
                new Label("Itinéraire proposé :"), listeItineraire,
                labelDistanceHeuristique);

        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(20));

        Scene scene = new Scene(layout, 800, 700);
        stage.setTitle("APPLI - Livraison de cartes Pokémon");
        stage.setScene(scene);
        stage.show();
    }
}
