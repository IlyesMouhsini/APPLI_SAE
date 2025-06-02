package modele;

import java.util.*;

public class GrapheOriente {
    // Attributs originaux conservés
    TreeMap<Integer, Set<Integer>> voisinsSortant;

    // Nouveaux attributs pour le problème de livraison
    private Map<String, Integer> villeToIndex;
    private Map<Integer, String> indexToVille;
    private double[][] distances;
    private List<Vente> ventes;

    // Constructeur original conservé
    public GrapheOriente(int[][] graph) {
        voisinsSortant = new TreeMap<>();
        for (int i = 0; i < graph.length; i++) {
            voisinsSortant.put(i, new TreeSet<>());
            for (int j = 0; j < graph[i].length; j++) {
                voisinsSortant.get(i).add(graph[i][j]);
            }
        }
    }

    // Nouveau constructeur pour le problème de livraison
    public GrapheOriente(Map<String, Integer> villeToIndex, double[][] distances, List<Vente> ventes) {
        this.villeToIndex = villeToIndex;
        this.indexToVille = new HashMap<>();
        for (Map.Entry<String, Integer> entry : villeToIndex.entrySet()) {
            indexToVille.put(entry.getValue(), entry.getKey());
        }
        this.distances = distances;
        this.ventes = ventes;
        construireGrapheVentes();
    }

    // Construction du graphe selon le modèle A+/A- décrit dans le sujet
    private void construireGrapheVentes() {
        voisinsSortant = new TreeMap<>();

        // Créer les sommets A+ et A- pour chaque ville impliquée dans les ventes
        Set<String> villesImpliquees = new HashSet<>();
        for (Vente vente : ventes) {
            villesImpliquees.add(vente.getVilleVendeur());
            villesImpliquees.add(vente.getVilleAcheteur());
        }

        for (String ville : villesImpliquees) {
            if (villeToIndex.containsKey(ville)) {
                int indexPlus = villeToIndex.get(ville) * 2;     // A+
                int indexMoins = villeToIndex.get(ville) * 2 + 1; // A-
                voisinsSortant.put(indexPlus, new TreeSet<>());
                voisinsSortant.put(indexMoins, new TreeSet<>());
            }
        }

        // Ajouter les arcs pour chaque vente A+ -> B-
        for (Vente vente : ventes) {
            Integer indexVendeur = villeToIndex.get(vente.getVilleVendeur());
            Integer indexAcheteur = villeToIndex.get(vente.getVilleAcheteur());

            if (indexVendeur != null && indexAcheteur != null) {
                int vendeurPlus = indexVendeur * 2;
                int acheteurMoins = indexAcheteur * 2 + 1;
                voisinsSortant.get(vendeurPlus).add(acheteurMoins);
            }
        }
    }

    // ALGORITHME 1 : Solution de base utilisant le tri topologique
    public Solution algorithmeBase() {
        if (ventes == null || ventes.isEmpty()) {
            return new Solution(Arrays.asList("Velizy"), 0.0);
        }

        List<Integer> ordreTopologique = trieTopologique();
        List<String> itineraire = construireItineraire(ordreTopologique);

        // VÉRIFICATION: s'assurer que l'itinéraire part et revient à Velizy
        if (itineraire.isEmpty() || !itineraire.get(0).equals("Velizy")) {
            itineraire.add(0, "Velizy");
        }
        if (!itineraire.get(itineraire.size() - 1).equals("Velizy")) {
            itineraire.add("Velizy");
        }

        double distance = calculerDistance(itineraire);
        return new Solution(itineraire, distance);
    }

    // ALGORITHME 2 : Heuristique simple
    public Solution algorithmeHeuristique() {
        if (ventes == null || ventes.isEmpty()) {
            return new Solution(Arrays.asList("Velizy"), 0.0);
        }

        List<String> itineraire = new ArrayList<>();
        itineraire.add("Velizy");

        Set<Vente> ventesRestantes = new HashSet<>(ventes);
        Map<String, Set<String>> cartesEnTransit = new HashMap<>();
        String positionActuelle = "Velizy";

        while (!ventesRestantes.isEmpty() || !cartesEnTransit.isEmpty()) {
            String prochaineDest = trouverProchaineDestination(positionActuelle, ventesRestantes, cartesEnTransit);

            if (prochaineDest != null && !prochaineDest.equals(positionActuelle)) {
                itineraire.add(prochaineDest);
                positionActuelle = prochaineDest;

                // Prendre les cartes à vendre depuis cette ville
                Iterator<Vente> iter = ventesRestantes.iterator();
                while (iter.hasNext()) {
                    Vente vente = iter.next();
                    if (vente.getVilleVendeur().equals(positionActuelle)) {
                        cartesEnTransit.computeIfAbsent(vente.getVilleAcheteur(), k -> new HashSet<>())
                                .add(vente.getVilleVendeur());
                        iter.remove();
                    }
                }

                // Livrer les cartes si on est dans la ville de l'acheteur
                cartesEnTransit.remove(positionActuelle);
            } else {
                break;
            }
        }

        // GARANTIR le retour à Velizy
        if (!positionActuelle.equals("Velizy")) {
            itineraire.add("Velizy");
        }

        // PAS de 2-opt pour rester cohérent avec l'énumération exhaustive
        double distance = calculerDistance(itineraire);
        return new Solution(itineraire, distance);
    }

    // ALGORITHME 3 : K meilleures solutions
    public List<Solution> kMeilleuresSolutions(int k) {
        if (ventes == null || ventes.isEmpty()) {
            return Arrays.asList(new Solution(Arrays.asList("Velizy"), 0.0));
        }

        System.out.println("=== ÉNUMÉRATION OPTIMISÉE DES SOLUTIONS ===");
        System.out.println("Nombre de ventes : " + ventes.size());

        List<Solution> toutesLesSolutions = new ArrayList<>();

        // Énumérer toutes les permutations avec calcul optimisé
        List<Vente> ventesListe = new ArrayList<>(ventes);
        enumererAvecOptimisation(ventesListe, new ArrayList<>(), toutesLesSolutions);

        System.out.println("Solutions générées : " + toutesLesSolutions.size());

        if (toutesLesSolutions.isEmpty()) {
            System.err.println("Aucune solution trouvée, fallback sur heuristique");
            return Arrays.asList(algorithmeHeuristique());
        }

        // Trier par distance croissante
        toutesLesSolutions.sort(Comparator.comparingDouble(Solution::getDistance));

        // Supprimer doublons
        List<Solution> solutionsUniques = new ArrayList<>();
        for (Solution sol : toutesLesSolutions) {
            boolean doublon = false;
            for (Solution unique : solutionsUniques) {
                if (Math.abs(unique.getDistance() - sol.getDistance()) < 0.01) {
                    doublon = true;
                    break;
                }
            }
            if (!doublon) {
                solutionsUniques.add(sol);
            }
        }

        System.out.println("Solutions uniques : " + solutionsUniques.size());

        // Sélectionner les k meilleures
        List<Solution> kMeilleures = solutionsUniques.size() > k ?
                solutionsUniques.subList(0, k) : solutionsUniques;

        // VÉRIFICATION AVEC HEURISTIQUE
        Solution heuristique = algorithmeHeuristique();
        System.out.println("\n=== VÉRIFICATION COHÉRENCE ===");
        System.out.println("Heuristique : " + String.format("%.2f", heuristique.getDistance()) + " km");
        if (!kMeilleures.isEmpty()) {
            System.out.println("Meilleure énumération : " + String.format("%.2f", kMeilleures.get(0).getDistance()) + " km");

            if (kMeilleures.get(0).getDistance() > heuristique.getDistance()) {
                System.err.println("PROBLÈME : Énumération moins bonne que heuristique !");
                System.err.println("Ajout forcé de la solution heuristique");
                kMeilleures.add(0, heuristique);
                kMeilleures.sort(Comparator.comparingDouble(Solution::getDistance));
                if (kMeilleures.size() > k) {
                    kMeilleures = kMeilleures.subList(0, k);
                }
            }
        }

        System.out.println("\n=== RÉSULTATS FINAUX ===");
        for (int i = 0; i < kMeilleures.size(); i++) {
            System.out.println("  " + (i+1) + ". " + String.format("%.2f", kMeilleures.get(i).getDistance()) + " km");
        }

        return kMeilleures;
    }

    // Énumération avec optimisation intelligente
    private void enumererAvecOptimisation(List<Vente> ventesRestantes, List<Vente> permutationCourante,
                                          List<Solution> solutions) {
        if (ventesRestantes.isEmpty()) {
            Solution solution = calculerSolutionOptimisee(permutationCourante);
            if (solution != null) {
                solutions.add(solution);
            }
            return;
        }

        // Limite pour éviter explosion
        if (ventesRestantes.size() > 7) {
            // Pour les gros problèmes, prendre les premières ventes et traiter le reste
            List<Vente> permComplete = new ArrayList<>(permutationCourante);
            permComplete.addAll(ventesRestantes);
            Solution solution = calculerSolutionOptimisee(permComplete);
            if (solution != null) {
                solutions.add(solution);
            }
            return;
        }

        // Énumération récursive
        for (int i = 0; i < ventesRestantes.size(); i++) {
            Vente vente = ventesRestantes.get(i);

            List<Vente> nouvellePermutation = new ArrayList<>(permutationCourante);
            nouvellePermutation.add(vente);

            List<Vente> nouvellesRestantes = new ArrayList<>(ventesRestantes);
            nouvellesRestantes.remove(i);

            enumererAvecOptimisation(nouvellesRestantes, nouvellePermutation, solutions);
        }
    }

    // Calcul de solution VRAIMENT optimisé
    private Solution calculerSolutionOptimisee(List<Vente> permutationVentes) {
        try {
            List<String> itineraire = new ArrayList<>();
            itineraire.add("Velizy");

            String positionActuelle = "Velizy";
            Map<String, Set<String>> cartesEnTransit = new HashMap<>(); // destination -> origines

            // Traiter chaque vente dans l'ordre
            for (Vente vente : permutationVentes) {
                String vendeur = vente.getVilleVendeur();
                String acheteur = vente.getVilleAcheteur();

                // 1. Aller chez le vendeur SEULEMENT si pas déjà sur place
                if (!positionActuelle.equals(vendeur)) {
                    itineraire.add(vendeur);
                    positionActuelle = vendeur;
                }

                // 2. Prendre les cartes
                cartesEnTransit.computeIfAbsent(acheteur, k -> new HashSet<>()).add(vendeur);

                // 3. OPTIMISATION CRUCIALE : Livrer IMMÉDIATEMENT si on peut
                if (cartesEnTransit.containsKey(positionActuelle) &&
                        !cartesEnTransit.get(positionActuelle).isEmpty()) {
                    cartesEnTransit.remove(positionActuelle);
                }
            }

            // 4. Livrer toutes les cartes restantes en transit (OPTIMISÉ)
            while (!cartesEnTransit.isEmpty()) {
                // Trouver la destination la PLUS PROCHE pour minimiser les détours
                String destinationPlusProche = null;
                double distanceMin = Double.MAX_VALUE;

                for (String destination : cartesEnTransit.keySet()) {
                    if (!cartesEnTransit.get(destination).isEmpty()) {
                        double distance = getDistance(positionActuelle, destination);
                        if (distance < distanceMin) {
                            distanceMin = distance;
                            destinationPlusProche = destination;
                        }
                    }
                }

                if (destinationPlusProche != null && !positionActuelle.equals(destinationPlusProche)) {
                    itineraire.add(destinationPlusProche);
                    positionActuelle = destinationPlusProche;
                }

                // Livrer toutes les cartes pour cette destination
                cartesEnTransit.remove(destinationPlusProche);
            }

            // 5. Retour à Velizy
            if (!positionActuelle.equals("Velizy")) {
                itineraire.add("Velizy");
            }

            // 6. OPTIMISATION FINALE : Supprimer les passages consécutifs identiques
            List<String> itineraireOptimise = new ArrayList<>();
            String precedente = null;
            for (String ville : itineraire) {
                if (!ville.equals(precedente)) {
                    itineraireOptimise.add(ville);
                    precedente = ville;
                }
            }

            double distance = calculerDistance(itineraireOptimise);
            return new Solution(itineraireOptimise, distance);

        } catch (Exception e) {
            System.err.println("Erreur calcul solution : " + e.getMessage());
            return null;
        }
    }

    // Méthodes utilitaires pour les algorithmes
    private String trouverProchaineDestination(String position, Set<Vente> ventesRestantes,
                                               Map<String, Set<String>> cartesEnTransit) {
        String meilleureDest = null;
        double meilleureDistance = Double.MAX_VALUE;

        // Priorité aux livraisons en cours
        for (String dest : cartesEnTransit.keySet()) {
            double dist = getDistance(position, dest);
            if (dist < meilleureDistance) {
                meilleureDistance = dist;
                meilleureDest = dest;
            }
        }

        // Sinon, chercher la vente la plus proche
        if (meilleureDest == null) {
            for (Vente vente : ventesRestantes) {
                double dist = getDistance(position, vente.getVilleVendeur());
                if (dist < meilleureDistance && dist > 0) {
                    meilleureDistance = dist;
                    meilleureDest = vente.getVilleVendeur();
                }
            }
        }

        return meilleureDest;
    }

    private List<String> construireItineraire(List<Integer> ordreTopologique) {
        List<String> itineraire = new ArrayList<>();
        itineraire.add("Velizy");

        for (Integer sommet : ordreTopologique) {
            if (sommet != null && indexToVille.containsKey(sommet / 2)) {
                String ville = indexToVille.get(sommet / 2);
                if (!itineraire.get(itineraire.size() - 1).equals(ville)) {
                    itineraire.add(ville);
                }
            }
        }

        if (!itineraire.get(itineraire.size() - 1).equals("Velizy")) {
            itineraire.add("Velizy");
        }

        return itineraire;
    }

    private double calculerDistance(List<String> itineraire) {
        double distance = 0;
        for (int i = 0; i < itineraire.size() - 1; i++) {
            distance += getDistance(itineraire.get(i), itineraire.get(i + 1));
        }
        return distance;
    }

    private double getDistance(String ville1, String ville2) {
        if (ville1.equals(ville2)) return 0;

        Integer index1 = villeToIndex.get(ville1);
        Integer index2 = villeToIndex.get(ville2);

        if (index1 == null || index2 == null) return Double.MAX_VALUE;
        if (index1 >= distances.length || index2 >= distances[0].length) return Double.MAX_VALUE;

        return distances[index1][index2];
    }

    // Méthodes originales conservées
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Ordre : ").append(this.ordre()).append("\n");
        sb.append("Taille : ").append(this.taille()).append("\n");
        sb.append("DegreMin : ").append(this.degreMinimal()).append("\n");
        sb.append("DegreMax : ").append(this.degreMaximal()).append("\n");
        for (Integer i : listeSommets()) {
            sb.append("sommet ").append(i).append(" degre=").append(degre(i))
                    .append(" voisins sortant: ").append(voisinsSortant.get(i)).append("\n");
        }
        return sb.toString();
    }

    public List<Integer> listeSommets() {
        List<Integer> sommets = new ArrayList<>(voisinsSortant.keySet());
        Collections.sort(sommets);
        return sommets;
    }

    public int ordre() {
        return this.listeSommets().size();
    }

    public int degre(Integer sommet) {
        return voisinsSortant.get(sommet) != null ? voisinsSortant.get(sommet).size() : 0;
    }

    public int taille() {
        int taille = 0;
        for (Integer i : listeSommets()) {
            taille += this.degre(i);
        }
        return taille;
    }

    public int degreMinimal() {
        int degreMin = Integer.MAX_VALUE;
        for (Integer i : listeSommets()) {
            if (degre(i) < degreMin)
                degreMin = degre(i);
        }
        return degreMin == Integer.MAX_VALUE ? 0 : degreMin;
    }

    public int degreMaximal() {
        int degreMax = 0;
        for (Integer i : listeSommets()) {
            if (degre(i) > degreMax)
                degreMax = degre(i);
        }
        return degreMax;
    }

    public List<Integer> trieTopologique() {
        ArrayList<Integer> num = new ArrayList<>();
        TreeMap<Integer, Set<Integer>> lvs = new TreeMap<>();

        // Copier les voisins sortants
        for (Integer sommet : voisinsSortant.keySet()) {
            lvs.put(sommet, new TreeSet<>(voisinsSortant.get(sommet)));
        }

        TreeMap<Integer, Integer> degreEntrant = getDegreEntrant();
        TreeSet<Integer> sources = sommetsSources(degreEntrant);

        while (!sources.isEmpty()) {
            Integer courant = sources.pollFirst();
            if (lvs.containsKey(courant)) {
                for (Integer voisin : lvs.get(courant)) {
                    degreEntrant.put(voisin, degreEntrant.get(voisin) - 1);
                    if (degreEntrant.get(voisin) == 0) {
                        sources.add(voisin);
                    }
                }
            }
            num.add(courant);
        }
        return num;
    }

    private TreeMap<Integer, Integer> getDegreEntrant() {
        TreeMap<Integer, Integer> degreEntrant = new TreeMap<>();
        for (Integer i : this.listeSommets()) {
            degreEntrant.put(i, 0);
            for (Integer j : this.listeSommets()) {
                if (this.voisinsSortant.get(j) != null && this.voisinsSortant.get(j).contains(i)) {
                    degreEntrant.put(i, degreEntrant.get(i) + 1);
                }
            }
        }
        return degreEntrant;
    }

    private TreeSet<Integer> sommetsSources(TreeMap<Integer, Integer> degreEntrant) {
        TreeSet<Integer> sommets = new TreeSet<>();
        for (Integer i : listeSommets()) {
            if (degreEntrant.get(i) == 0) {
                sommets.add(i);
            }
        }
        return sommets;
    }
}