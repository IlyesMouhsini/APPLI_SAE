package controleur;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import modele.*;

import java.io.IOException;
import java.util.List;
import java.util.Map;

public class ControleurAccueil {

    public void chargerScenario(
            ComboBox<String> comboScenarios,
            ListView<String> listeVentes,
            ListView<String> listeItineraire,
            Label labelDistance,
            Label labelDistanceHeuristique) {

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
