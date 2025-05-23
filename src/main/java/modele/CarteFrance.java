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
}
