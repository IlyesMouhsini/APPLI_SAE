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


public class ControleurAccueil {

    @FXML
    private ListView<String> listeVentes;

    @FXML
    private ComboBox<String> comboScenarios;

    @FXML
    public void initialize() {
        // Remplir la combo avec les noms des fichiers de /scenarios/
        try {
            // Récupérer le dossier dans le classpath
            URI uri = getClass().getResource("/scenarios/").toURI();
            Path path = Paths.get(uri);

            Files.list(path)
                    .filter(f -> f.getFileName().toString().endsWith(".txt"))
                    .forEach(f -> comboScenarios.getItems().add(f.getFileName().toString()));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    @FXML
    private void handleChargerScenario() {
        String nomFichier = comboScenarios.getValue();
        if (nomFichier == null) return;

        List<Vente> ventes = lireScenario("/scenarios/" + nomFichier);

        ObservableList<String> affichage = FXCollections.observableArrayList();
        for (Vente v : ventes) {
            affichage.add(v.toString());
        }

        listeVentes.setItems(affichage);
    }


    private List<Vente> lireScenario(String nomFichier) {
        List<Vente> ventes = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(getClass().getResourceAsStream(nomFichier), StandardCharsets.UTF_8))) {

            String ligne;
            while ((ligne = reader.readLine()) != null) {
                if (ligne.contains("->")) {
                    String[] parts = ligne.split("->");
                    String vendeur = parts[0].trim();
                    String acheteur = parts[1].trim();
                    ventes.add(new Vente(vendeur, acheteur));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return ventes;
    }
}
