package modele;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class Scenario {
    private List<Vente> ventes;

    public Scenario() {
        this.ventes = new ArrayList<>();
    }

    public void ajouterVente(Vente vente) {
        ventes.add(vente);
    }

    public List<Vente> getVentes() {
        return ventes;
    }

    public int calculerDistanceTotale(CarteFrance carte) {
        int distanceTotale = 0;
        String villeActuelle = "Velizy";

        for (Vente vente : ventes) {
            String villeVendeur = vente.getVendeur().getVille();
            String villeAcheteur = vente.getAcheteur().getVille();

            distanceTotale += carte.getDistance(villeActuelle, villeVendeur);
            distanceTotale += carte.getDistance(villeVendeur, villeAcheteur);

            villeActuelle = villeAcheteur;
        }

        distanceTotale += carte.getDistance(villeActuelle, "Velizy");

        return distanceTotale;
    }

    public static Scenario chargerDepuisFichier(String nomFichier, Map<String, Membre> membresMap) throws IOException {
        Scenario scenario = new Scenario();
        String chemin = "src/main/resources/scenarios/" + nomFichier;

        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                if (ligne.contains("->")) {
                    String[] parties = ligne.split("->");
                    String pseudoVendeur = parties[0].trim();
                    String pseudoAcheteur = parties[1].trim();

                    Membre vendeur = membresMap.get(pseudoVendeur);
                    Membre acheteur = membresMap.get(pseudoAcheteur);

                    if (vendeur != null && acheteur != null) {
                        scenario.ajouterVente(new Vente(vendeur, acheteur));
                    } else {
                        System.err.println("Membre introuvable pour la vente : " + ligne);
                    }
                }
            }
        }
        return scenario;
    }

    public static Map<String, Membre> chargerMembres(String nomFichier) throws IOException {
        Map<String, Membre> membresMap = new HashMap<>();
        String chemin = "src/main/resources/membres/" + nomFichier;

        try (BufferedReader br = new BufferedReader(new FileReader(chemin))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                String[] parties = ligne.split(" ");
                if (parties.length >= 2) {
                    String pseudo = parties[0];
                    String ville = parties[1];
                    membresMap.put(pseudo, new Membre(pseudo, ville));
                }
            }
        }
        return membresMap;
    }
}
