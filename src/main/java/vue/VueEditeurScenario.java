package vue;

import controleur.ControleurAccueil;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import modele.*;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class VueEditeurScenario {

    private EditeurScenario editeur;
    private ListView<String> listeVentes;
    private ComboBox<String> comboVendeurs;
    private ComboBox<String> comboAcheteurs;
    private TextField champNomFichier;
    private ComboBox<String> comboChargerScenario;
    private Map<String, Membre> membres;
    private Label labelConfirmation;

    public void start(Stage stage) {
        try {
            membres = Scenario.chargerMembres("membres_APPLI.txt");
            Scenario scenario = new Scenario();
            editeur = new EditeurScenario(scenario);

            comboVendeurs = new ComboBox<>();
            comboAcheteurs = new ComboBox<>();
            membres.keySet().forEach(pseudo -> {
                comboVendeurs.getItems().add(pseudo);
                comboAcheteurs.getItems().add(pseudo);
            });

            Button ajouter = new Button("Ajouter vente");
            ajouter.setOnAction(e -> {
                Membre v = membres.get(comboVendeurs.getValue());
                Membre a = membres.get(comboAcheteurs.getValue());
                if (v != null && a != null) {
                    editeur.ajouterVente(v, a);
                    rafraichirListe();
                }
            });

            Button supprimer = new Button("Supprimer vente sélectionnée");
            supprimer.setOnAction(e -> {
                int index = listeVentes.getSelectionModel().getSelectedIndex();
                editeur.supprimerVente(index);
                rafraichirListe();
            });

            champNomFichier = new TextField();
            champNomFichier.setPromptText("Nom du scénario .txt");
            Button sauvegarder = new Button("Sauvegarder");
            labelConfirmation = new Label();
            sauvegarder.setOnAction(e -> {
                try {
                    String nom = champNomFichier.getText();
                    if (!nom.endsWith(".txt")) {
                        nom += ".txt";
                    }
                    File f = new File(System.getProperty("user.dir") + "/scenarios/" + nom);
                    if (f.exists()) {
                        labelConfirmation.setText("Erreur : un scénario avec ce nom existe déjà");
                    } else {
                        editeur.sauvegarder(nom);
                        labelConfirmation.setText("Scénario sauvegardé!");
                        comboChargerScenario.setItems(FXCollections.observableArrayList(ScenarioUtils.listerScenarios()));
                    }
                } catch (IOException ex) {
                    labelConfirmation.setText("Erreur : " + ex.getMessage());
                    ex.printStackTrace();
                }
            });

            comboChargerScenario = new ComboBox<>();
            comboChargerScenario.setPromptText("Charger un scénario existant");
            comboChargerScenario.setItems(FXCollections.observableArrayList(ScenarioUtils.listerScenarios()));

            Button charger = new Button("Charger");
            charger.setOnAction(e -> {
                String nom = comboChargerScenario.getValue();
                if (nom != null) {
                    try {
                        Scenario sc = Scenario.chargerDepuisFichier(nom, membres);
                        editeur = new EditeurScenario(sc);
                        rafraichirListe();
                        champNomFichier.setText(nom);
                        labelConfirmation.setText("Scénario chargé ✔");
                    } catch (IOException ex) {
                        labelConfirmation.setText("Erreur : " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }
            });

            listeVentes = new ListView<>();
            listeVentes.setPrefHeight(200);

            VBox root = new VBox(10,
                    new Label("Créer ou modifier un scénario"),
                    new HBox(10, new Label("Vendeur:"), comboVendeurs, new Label("Acheteur:"), comboAcheteurs, ajouter),
                    new HBox(10, comboChargerScenario, charger),
                    listeVentes,
                    supprimer,
                    new HBox(10, champNomFichier, sauvegarder),
                    labelConfirmation
            );

            root.setPadding(new Insets(20));
            root.setAlignment(Pos.CENTER);

            Scene scene = new Scene(root, 700, 450);
            stage.setTitle("Éditeur de scénario");
            stage.setScene(scene);
            stage.show();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void rafraichirListe() {
        ObservableList<String> data = FXCollections.observableArrayList();
        for (Vente v : editeur.getScenario().getVentes()) {
            data.add(v.toString());
        }
        listeVentes.setItems(data);
    }
}
