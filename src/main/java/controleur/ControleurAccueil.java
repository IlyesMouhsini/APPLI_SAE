package controleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import modele.Vente;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javafx.scene.control.ComboBox;
import java.net.URI;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import javafx.scene.control.Label;
import modele.CarteFrance;


import modele.Membre;
import modele.Vente;
import modele.Scenario;

public class ControleurAccueil {

    @FXML
    private ListView<String> listeVentes;

    @FXML
    private ComboBox<String> comboScenarios;

    @FXML
    private Label labelDistance;


    @FXML
    public void initialize() {
        try {
            URI uri = getClass().getClassLoader().getResource("scenarios").toURI();
            Path dossierScenarios = Paths.get(uri);
            List<String> fichiers = new ArrayList<>();

            Files.list(dossierScenarios)
                    .filter(path -> path.toString().endsWith(".txt"))
                    .forEach(path -> fichiers.add(path.getFileName().toString()));

            comboScenarios.setItems(FXCollections.observableArrayList(fichiers));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void handleChargerScenario(ActionEvent event) {
        String nomScenario = comboScenarios.getValue();
        if (nomScenario == null) return;

        try {
            // Chargement des membres
            Map<String, Membre> membres = Scenario.chargerMembres("membres_APPLI.txt");

            // Chargement du scénario sélectionné
            Scenario scenario = Scenario.chargerDepuisFichier(nomScenario, membres);

            // Affichage des ventes dans la liste
            ObservableList<String> affichage = FXCollections.observableArrayList();
            for (Vente v : scenario.getVentes()) {
                affichage.add(v.toString());
            }
            listeVentes.setItems(affichage);

            // Chargement de la carte de France (distances)
            CarteFrance carte = new CarteFrance("src/main/resources/distances/distances.txt");

            // Calcul de la distance totale du scénario
            int total = scenario.calculerDistanceTotale(carte);

            // Affichage de la distance dans le label
            labelDistance.setText("Distance totale : " + total + " km");

        } catch (IOException e) {
            labelDistance.setText("Erreur : " + e.getMessage());
            e.printStackTrace();
        }
    }

}