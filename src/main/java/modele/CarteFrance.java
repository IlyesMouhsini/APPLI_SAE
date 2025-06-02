package modele;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

public class CarteFrance {
    private final List<String> villes;
    private final Map<String, Integer> indexVille;
    private final int[][] distances;

    public CarteFrance(String cheminFichier) throws IOException {
        villes = new ArrayList<>();
        indexVille = new HashMap<>();

        try (BufferedReader br = new BufferedReader(new FileReader(cheminFichier))) {
            String ligne;
            List<int[]> lignesDistances = new ArrayList<>();

            while ((ligne = br.readLine()) != null) {
                String[] parties = ligne.trim().split("\\s+");
                if (parties.length < 2) continue;

                String ville = parties[0];
                villes.add(ville);
                indexVille.put(ville, villes.size() - 1);

                int[] dists = new int[parties.length - 1];
                for (int i = 1; i < parties.length; i++) {
                    dists[i - 1] = Integer.parseInt(parties[i]);
                }
                lignesDistances.add(dists);
            }

            distances = lignesDistances.toArray(new int[0][]);
        }
    }

    public int getDistance(String villeA, String villeB) {
        Integer i = indexVille.get(villeA);
        Integer j = indexVille.get(villeB);
        if (i == null || j == null) {
            throw new IllegalArgumentException("Ville inconnue : " + villeA + " ou " + villeB);
        }
        return distances[i][j];
    }

    public List<String> getVilles() {
        return Collections.unmodifiableList(villes);
    }

    // NOUVELLES MÉTHODES AJOUTÉES POUR LE GRAPHE ORIENTÉ

    /**
     * Retourne la map associant chaque ville à son index
     * @return Map<String, Integer> ville -> index
     */
    public Map<String, Integer> getVilleToIndex() {
        return Collections.unmodifiableMap(indexVille);
    }

    /**
     * Retourne la matrice des distances
     * @return int[][] matrice des distances
     */
    public int[][] getDistances() {
        // Retourner une copie pour éviter les modifications externes
        int[][] copie = new int[distances.length][];
        for (int i = 0; i < distances.length; i++) {
            copie[i] = distances[i].clone();
        }
        return copie;
    }

    /**
     * Retourne la map associant chaque index à sa ville (utile pour certains algorithmes)
     * @return Map<Integer, String> index -> ville
     */
    public Map<Integer, String> getIndexToVille() {
        Map<Integer, String> indexToVille = new HashMap<>();
        for (Map.Entry<String, Integer> entry : indexVille.entrySet()) {
            indexToVille.put(entry.getValue(), entry.getKey());
        }
        return indexToVille;
    }

    /**
     * Vérifie si une ville existe dans la carte
     * @param ville nom de la ville
     * @return true si la ville existe, false sinon
     */
    public boolean villeExiste(String ville) {
        return indexVille.containsKey(ville);
    }

    /**
     * Retourne le nombre total de villes
     * @return nombre de villes
     */
    public int getNombreVilles() {
        return villes.size();
    }
}