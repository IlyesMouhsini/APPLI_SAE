package modele;

import java.util.*;

public class Graphe {

    /**
     * Algorithme heuristique amélioré : à chaque étape, on choisit le vendeur
     * le plus proche de la position actuelle, puis on livre tous ses acheteurs sans revisiter les villes déjà incluses.
     */
    public static List<String> calculerItineraireHeuristique(Scenario scenario, CarteFrance carte) {
        List<String> itineraire = new ArrayList<>();
        String villeActuelle = "Velizy";
        itineraire.add(villeActuelle);

        // Regrouper les ventes par vendeur
        Map<String, List<Vente>> ventesParVendeur = new HashMap<>();
        for (Vente vente : scenario.getVentes()) {
            String vendeurVille = vente.getVendeur().getVille();
            ventesParVendeur.computeIfAbsent(vendeurVille, k -> new ArrayList<>()).add(vente);
        }

        Set<String> vendeursVisites = new HashSet<>();

        while (vendeursVisites.size() < ventesParVendeur.size()) {
            String prochainVendeur = null;
            int minDistance = Integer.MAX_VALUE;

            for (String vendeurVille : ventesParVendeur.keySet()) {
                if (!vendeursVisites.contains(vendeurVille)) {
                    int distance = carte.getDistance(villeActuelle, vendeurVille);
                    if (distance < minDistance) {
                        minDistance = distance;
                        prochainVendeur = vendeurVille;
                    }
                }
            }

            if (prochainVendeur != null) {
                if (!itineraire.contains(prochainVendeur)) {
                    itineraire.add(prochainVendeur);
                }
                for (Vente vente : ventesParVendeur.get(prochainVendeur)) {
                    String acheteurVille = vente.getAcheteur().getVille();
                    if (!itineraire.contains(acheteurVille)) {
                        itineraire.add(acheteurVille);
                    }
                    villeActuelle = acheteurVille;
                }
                vendeursVisites.add(prochainVendeur);
            } else {
                break;
            }
        }

        if (!villeActuelle.equals("Velizy")) {
            itineraire.add("Velizy");
        }


        return itineraire;
    }
}
