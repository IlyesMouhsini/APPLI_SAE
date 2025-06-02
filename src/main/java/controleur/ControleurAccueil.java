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
        if (nomScenario == null) {
            labelDistance.setText("Aucun scénario sélectionné");
            labelDistanceHeuristique.setText("Aucun scénario sélectionné");
            return;
        }

        try {
            System.out.println("Chargement du scénario : " + nomScenario);

            // CORRECTION DES CHEMINS : utiliser les chemins correspondant à votre structure
            Map<String, Membre> membres = Scenario.chargerMembres("membres_APPLI.txt");
            System.out.println("Nombre de membres chargés : " + membres.size());

            Scenario scenario = Scenario.chargerDepuisFichier(nomScenario, membres);
            System.out.println("Nombre de ventes dans le scénario : " + scenario.getVentes().size());

            // Afficher les ventes dans la liste
            ObservableList<String> affichage = FXCollections.observableArrayList();
            for (Vente v : scenario.getVentes()) {
                affichage.add(v.toString());
            }
            listeVentes.setItems(affichage);

            // Vérifier si le scénario contient des ventes
            if (scenario.getVentes().isEmpty()) {
                labelDistance.setText("Aucune vente dans ce scénario");
                labelDistanceHeuristique.setText("Aucune vente dans ce scénario");
                listeItineraire.setItems(FXCollections.observableArrayList("Vélizy"));
                return;
            }

            // CORRECTION : utiliser le bon chemin pour les distances
            CarteFrance carte = new CarteFrance("src/main/resources/distances/distances.txt");
            System.out.println("Carte chargée avec " + carte.getNombreVilles() + " villes");

            // Vérifier que toutes les villes des ventes existent dans la carte
            boolean villesManquantes = false;
            for (Vente vente : scenario.getVentes()) {
                if (!carte.villeExiste(vente.getVilleVendeur())) {
                    System.err.println("Ville manquante : " + vente.getVilleVendeur());
                    villesManquantes = true;
                }
                if (!carte.villeExiste(vente.getVilleAcheteur())) {
                    System.err.println("Ville manquante : " + vente.getVilleAcheteur());
                    villesManquantes = true;
                }
            }

            if (villesManquantes) {
                labelDistance.setText("Erreur : Villes manquantes dans la carte");
                labelDistanceHeuristique.setText("Erreur : Villes manquantes dans la carte");
                return;
            }

            // Créer le graphe orienté avec les données nécessaires
            GrapheOriente graphe = new GrapheOriente(
                    carte.getVilleToIndex(),
                    convertirDistancesIntToDouble(carte.getDistances()),
                    scenario.getVentes()
            );

            // ALGORITHME 1 : Solution de base
            Solution solutionBase = graphe.algorithmeBase();
            labelDistance.setText("Distance totale : " +
                    String.format("%.2f", solutionBase.getDistance()) + " km");

            // ALGORITHME 2 : Solution heuristique
            Solution solutionHeuristique = graphe.algorithmeHeuristique();
            listeItineraire.setItems(FXCollections.observableArrayList(solutionHeuristique.getItineraire()));
            labelDistanceHeuristique.setText("Distance heuristique : " +
                    String.format("%.2f", solutionHeuristique.getDistance()) + " km");

            System.out.println("Calculs terminés avec succès");

        } catch (IOException e) {
            String erreur = "Erreur de fichier : " + e.getMessage();
            labelDistance.setText(erreur);
            labelDistanceHeuristique.setText(erreur);
            System.err.println("Erreur IOException: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            String erreur = "Erreur de calcul : " + e.getMessage();
            labelDistance.setText(erreur);
            labelDistanceHeuristique.setText(erreur);
            System.err.println("Erreur générale: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Convertit une matrice d'entiers en matrice de doubles
     */
    private double[][] convertirDistancesIntToDouble(int[][] distancesInt) {
        double[][] distancesDouble = new double[distancesInt.length][];
        for (int i = 0; i < distancesInt.length; i++) {
            distancesDouble[i] = new double[distancesInt[i].length];
            for (int j = 0; j < distancesInt[i].length; j++) {
                distancesDouble[i][j] = (double) distancesInt[i][j];
            }
        }
        return distancesDouble;
    }

    public List<Solution> obtenirKMeilleuresSolutions(String nomScenario, int k) {
        try {
            Map<String, Membre> membres = Scenario.chargerMembres("membres_APPLI.txt");
            Scenario scenario = Scenario.chargerDepuisFichier(nomScenario, membres);
            CarteFrance carte = new CarteFrance("src/main/resources/distances/distances.txt");

            GrapheOriente graphe = new GrapheOriente(
                    carte.getVilleToIndex(),
                    convertirDistancesIntToDouble(carte.getDistances()),
                    scenario.getVentes()
            );

            return graphe.kMeilleuresSolutions(k);

        } catch (IOException e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void chargerToutesSolutions(
            String nomScenario,
            ListView<String> listeSolutions,
            int k) {

        try {
            Map<String, Membre> membres = Scenario.chargerMembres("membres_APPLI.txt");
            Scenario scenario = Scenario.chargerDepuisFichier(nomScenario, membres);
            CarteFrance carte = new CarteFrance("src/main/resources/distances/distances.txt");

            GrapheOriente graphe = new GrapheOriente(
                    carte.getVilleToIndex(),
                    convertirDistancesIntToDouble(carte.getDistances()),
                    scenario.getVentes()
            );

            ObservableList<String> affichageSolutions = FXCollections.observableArrayList();

            // Solution de base
            Solution solutionBase = graphe.algorithmeBase();
            affichageSolutions.add("ALGORITHME BASE: " + solutionBase.toString());

            // Solution heuristique
            Solution solutionHeuristique = graphe.algorithmeHeuristique();
            affichageSolutions.add("ALGORITHME HEURISTIQUE: " + solutionHeuristique.toString());

            // K meilleures solutions
            List<Solution> kMeilleures = graphe.kMeilleuresSolutions(k);
            affichageSolutions.add("--- " + k + " MEILLEURES SOLUTIONS ---");
            for (int i = 0; i < kMeilleures.size(); i++) {
                affichageSolutions.add("Solution " + (i + 1) + ": " + kMeilleures.get(i).toString());
            }

            listeSolutions.setItems(affichageSolutions);

        } catch (IOException e) {
            ObservableList<String> erreur = FXCollections.observableArrayList();
            erreur.add("Erreur lors du chargement : " + e.getMessage());
            listeSolutions.setItems(erreur);
            e.printStackTrace();
        }
    }

    // === AJOUTEZ CES DEUX MÉTHODES À VOTRE CLASSE ControleurAccueil ===

    /**
     * NOUVELLE MÉTHODE 1 : Calculer et afficher les K meilleures solutions
     * À appeler depuis l'interface quand l'utilisateur clique sur "Calculer k meilleures solutions"
     */
    public void calculerKMeilleuresSolutions(
            ComboBox<String> comboScenarios,
            ListView<String> listeSolutions,
            int k) {

        String nomScenario = comboScenarios.getValue();
        if (nomScenario == null) {
            listeSolutions.setItems(FXCollections.observableArrayList("Aucun scénario sélectionné"));
            return;
        }

        try {
            // Charger les données
            Map<String, Membre> membres = Scenario.chargerMembres("membres_APPLI.txt");
            Scenario scenario = Scenario.chargerDepuisFichier(nomScenario, membres);
            CarteFrance carte = new CarteFrance("src/main/resources/distances/distances.txt");

            GrapheOriente graphe = new GrapheOriente(
                    carte.getVilleToIndex(),
                    convertirDistancesIntToDouble(carte.getDistances()),
                    scenario.getVentes()
            );

            // ====== ALGORITHME 3 : K MEILLEURES SOLUTIONS ======
            List<Solution> kMeilleures = graphe.kMeilleuresSolutions(k);

            ObservableList<String> affichageSolutions = FXCollections.observableArrayList();
            affichageSolutions.add("=== " + k + " MEILLEURES SOLUTIONS (par distance croissante) ===");
            affichageSolutions.add("");

            if (kMeilleures.isEmpty()) {
                affichageSolutions.add("Aucune solution trouvée");
            } else {
                for (int i = 0; i < kMeilleures.size(); i++) {
                    Solution sol = kMeilleures.get(i);
                    affichageSolutions.add("SOLUTION " + (i + 1) + " :");
                    affichageSolutions.add("  Distance: " + String.format("%.2f", sol.getDistance()) + " km");
                    affichageSolutions.add("  Itinéraire: " + String.join(" -> ", sol.getItineraire()));
                    affichageSolutions.add(""); // ligne vide pour séparer
                }
            }

            listeSolutions.setItems(affichageSolutions);

        } catch (IOException e) {
            ObservableList<String> erreur = FXCollections.observableArrayList();
            erreur.add("Erreur lors du chargement : " + e.getMessage());
            listeSolutions.setItems(erreur);
            e.printStackTrace();
        }
    }

    /**
     * NOUVELLE MÉTHODE 2 : Afficher tous les algorithmes ensemble
     * À appeler depuis l'interface quand l'utilisateur clique sur "Comparer tous les algorithmes"
     */
    public void calculerToutesLesSolutions(
            ComboBox<String> comboScenarios,
            ListView<String> listeSolutions,
            int k) {

        String nomScenario = comboScenarios.getValue();
        if (nomScenario == null) {
            listeSolutions.setItems(FXCollections.observableArrayList("Aucun scénario sélectionné"));
            return;
        }

        try {
            Map<String, Membre> membres = Scenario.chargerMembres("membres_APPLI.txt");
            Scenario scenario = Scenario.chargerDepuisFichier(nomScenario, membres);
            CarteFrance carte = new CarteFrance("src/main/resources/distances/distances.txt");

            GrapheOriente graphe = new GrapheOriente(
                    carte.getVilleToIndex(),
                    convertirDistancesIntToDouble(carte.getDistances()),
                    scenario.getVentes()
            );

            ObservableList<String> affichageComplet = FXCollections.observableArrayList();

            // ALGORITHME 1
            Solution solutionBase = graphe.algorithmeBase();
            affichageComplet.add("=== ALGORITHME 1 : BASE (Tri topologique) ===");
            affichageComplet.add("Distance: " + String.format("%.2f", solutionBase.getDistance()) + " km");
            affichageComplet.add("Itinéraire: " + String.join(" -> ", solutionBase.getItineraire()));
            affichageComplet.add("");

            // ALGORITHME 2
            Solution solutionHeuristique = graphe.algorithmeHeuristique();
            affichageComplet.add("=== ALGORITHME 2 : HEURISTIQUE (Plus proche voisin + 2-opt) ===");
            affichageComplet.add("Distance: " + String.format("%.2f", solutionHeuristique.getDistance()) + " km");
            affichageComplet.add("Itinéraire: " + String.join(" -> ", solutionHeuristique.getItineraire()));
            affichageComplet.add("");

            // ALGORITHME 3
            List<Solution> kMeilleures = graphe.kMeilleuresSolutions(k);
            affichageComplet.add("=== ALGORITHME 3 : " + k + " MEILLEURES SOLUTIONS (Énumération contrôlée) ===");
            for (int i = 0; i < kMeilleures.size(); i++) {
                Solution sol = kMeilleures.get(i);
                affichageComplet.add("Solution " + (i + 1) + ": " +
                        String.format("%.2f", sol.getDistance()) + " km - " +
                        String.join(" -> ", sol.getItineraire()));
            }

            listeSolutions.setItems(affichageComplet);

        } catch (IOException e) {
            ObservableList<String> erreur = FXCollections.observableArrayList();
            erreur.add("Erreur lors du chargement : " + e.getMessage());
            listeSolutions.setItems(erreur);
            e.printStackTrace();
        }
    }
}