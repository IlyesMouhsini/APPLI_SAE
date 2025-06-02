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
        double distance = calculerDistance(itineraire);
        return new Solution(itineraire, distance);
    }

    // ALGORITHME 2 : Heuristique du plus proche voisin avec optimisations locales
    public Solution algorithmeHeuristique() {
        if (ventes == null || ventes.isEmpty()) {
            return new Solution(Arrays.asList("Velizy"), 0.0);
        }

        List<String> itineraire = new ArrayList<>();
        itineraire.add("Velizy");

        Set<Vente> ventesRestantes = new HashSet<>(ventes);
        Map<String, Set<String>> cartesEnTransit = new HashMap<>(); // ville -> destinations
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
                break; // Éviter les boucles infinies
            }
        }

        if (!positionActuelle.equals("Velizy")) {
            itineraire.add("Velizy");
        }

        // Optimisation locale : 2-opt
        itineraire = optimisation2Opt(itineraire);

        double distance = calculerDistance(itineraire);
        return new Solution(itineraire, distance);
    }

    // ALGORITHME 3 : K meilleures solutions par énumération contrôlée
    public List<Solution> kMeilleuresSolutions(int k) {
        if (ventes == null || ventes.isEmpty()) {
            return Arrays.asList(new Solution(Arrays.asList("Velizy"), 0.0));
        }

        List<Solution> solutions = new ArrayList<>();
        PriorityQueue<SolutionPartielle> queue = new PriorityQueue<>(
                Comparator.comparingDouble(SolutionPartielle::getDistanceEstimee)
        );

        // Initialiser avec Velizy
        SolutionPartielle initial = new SolutionPartielle();
        initial.ajouterVille("Velizy");
        queue.add(initial);

        int maxIterations = 10000; // Limite pour éviter l'explosion combinatoire
        int iterations = 0;

        while (!queue.isEmpty() && solutions.size() < k && iterations < maxIterations) {
            SolutionPartielle courante = queue.poll();
            iterations++;

            if (courante.estComplete(ventes)) {
                // Solution complète trouvée
                List<String> itineraire = new ArrayList<>(courante.getItineraire());
                if (!itineraire.get(itineraire.size() - 1).equals("Velizy")) {
                    itineraire.add("Velizy");
                }
                double distance = calculerDistance(itineraire);
                solutions.add(new Solution(itineraire, distance));

                // Trier les solutions par distance croissante
                solutions.sort(Comparator.comparingDouble(Solution::getDistance));
            } else {
                // Générer les successeurs
                Set<String> villesPossibles = courante.getVillesPossibles(ventes);
                for (String ville : villesPossibles) {
                    SolutionPartielle successeur = courante.copier();
                    successeur.ajouterVille(ville);
                    successeur.mettreAJourEtat(ville, ventes);

                    if (successeur.getDistanceEstimee() <= getBorneSuperieure(solutions, k)) {
                        queue.add(successeur);
                    }
                }
            }
        }

        return solutions.size() > k ? solutions.subList(0, k) : solutions;
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

    private List<String> optimisation2Opt(List<String> itineraire) {
        if (itineraire.size() < 4) return itineraire;

        List<String> meilleur = new ArrayList<>(itineraire);
        boolean amelioration = true;
        int maxIterations = 100;
        int iterations = 0;

        while (amelioration && iterations < maxIterations) {
            amelioration = false;
            iterations++;

            for (int i = 1; i < itineraire.size() - 2; i++) {
                for (int j = i + 1; j < itineraire.size() - 1; j++) {
                    List<String> nouveau = echange2Opt(meilleur, i, j);
                    if (calculerDistance(nouveau) < calculerDistance(meilleur)) {
                        meilleur = nouveau;
                        amelioration = true;
                    }
                }
            }
        }
        return meilleur;
    }

    private List<String> echange2Opt(List<String> itineraire, int i, int j) {
        List<String> nouveau = new ArrayList<>();
        nouveau.addAll(itineraire.subList(0, i));

        List<String> segment = new ArrayList<>(itineraire.subList(i, j + 1));
        Collections.reverse(segment);
        nouveau.addAll(segment);

        nouveau.addAll(itineraire.subList(j + 1, itineraire.size()));
        return nouveau;
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

    private double getBorneSuperieure(List<Solution> solutions, int k) {
        if (solutions.isEmpty()) return Double.MAX_VALUE;
        if (solutions.size() < k) return Double.MAX_VALUE;
        return solutions.get(k - 1).getDistance() * 1.2; // Marge de sécurité
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