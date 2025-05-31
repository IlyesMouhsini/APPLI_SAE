package vue;

import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modele.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class VueAccueil {

    private ComboBox<String> comboScenarios;
    private ListView<String> listeVentes;
    private ListView<String> listeItineraire;
    private Label labelDistance;
    private Label labelDistanceHeuristique;

    public void start(Stage stage) {
        Label titre = new Label("Bienvenue dans l'application APPLI");

        comboScenarios = new ComboBox<>();
        comboScenarios.setPromptText("Choisir un scénario");
        comboScenarios.setItems(FXCollections.observableArrayList(ScenarioUtils.listerScenarios()));

        Button boutonCharger = new Button("Charger ce scénario");
        boutonCharger.setOnAction(e -> chargerScenario());

        labelDistance = new Label("Distance totale : -");
        labelDistanceHeuristique = new Label("Distance heuristique : -");

        listeVentes = new ListView<>();
        listeVentes.setPrefHeight(150);

        listeItineraire = new ListView<>();
        listeItineraire.setPrefHeight(150);

        VBox layout = new VBox(10, titre, comboScenarios, boutonCharger,
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

    private void chargerScenario() {
        String nomScenario = comboScenarios.getValue();
        if (nomScenario == null) return;

        try {
            Map<String, Membre> membres = Scenario.chargerMembres("membres_APPLI.txt");
            Scenario scenario = Scenario.chargerDepuisFichier(nomScenario, membres);

            ObservableList<String> affichage = FXCollections.observableArrayList();
            for (Vente v : scenario.getVentes()) {
                affichage.add(v.toString());
            }
            listeVentes.setItems(affichage);

            CarteFrance carte = new CarteFrance("src/main/resources/distances/distances.txt");
            int total = scenario.calculerDistanceTotale(carte);
            labelDistance.setText("Distance totale : " + total + " km");

            List<String> itineraire = Graphe.calculerItineraireHeuristique(scenario, carte);
            listeItineraire.setItems(FXCollections.observableArrayList(itineraire));

            int heuristiqueTotal = 0;
            for (int i = 0; i < itineraire.size() - 1; i++) {
                heuristiqueTotal += carte.getDistance(itineraire.get(i), itineraire.get(i + 1));
            }
            labelDistanceHeuristique.setText("Distance heuristique : " + heuristiqueTotal + " km");

        } catch (IOException e) {
            labelDistance.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }
}
