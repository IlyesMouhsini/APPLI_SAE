package modele;

import java.util.*;

public class SolutionPartielle {
    private List<String> itineraire;
    private Set<Vente> ventesEffectuees;
    private Set<String> cartesEnTransit;

    public SolutionPartielle() {
        this.itineraire = new ArrayList<>();
        this.ventesEffectuees = new HashSet<>();
        this.cartesEnTransit = new HashSet<>();
    }

    public void ajouterVille(String ville) {
        itineraire.add(ville);
    }

    public boolean estComplete(List<Vente> toutesVentes) {
        return ventesEffectuees.size() == toutesVentes.size() && cartesEnTransit.isEmpty();
    }

    public Set<String> getVillesPossibles(List<Vente> ventes) {
        Set<String> possibles = new HashSet<>();

        // 1. Priorité aux livraisons en cours (cartes en transit)
        possibles.addAll(cartesEnTransit);

        // 2. Ajouter les villes de ventes NON ENCORE effectuées
        for (Vente vente : ventes) {
            if (!ventesEffectuees.contains(vente)) {
                possibles.add(vente.getVilleVendeur()); // Ville où prendre les cartes
            }
        }

        // 3. Retirer la ville actuelle si on y est déjà
        if (!itineraire.isEmpty()) {
            String villeActuelle = itineraire.get(itineraire.size() - 1);
            possibles.remove(villeActuelle);
        }

        return possibles;
    }

    public SolutionPartielle copier() {
        SolutionPartielle copie = new SolutionPartielle();
        copie.itineraire = new ArrayList<>(this.itineraire);
        copie.ventesEffectuees = new HashSet<>(this.ventesEffectuees);
        copie.cartesEnTransit = new HashSet<>(this.cartesEnTransit);
        return copie;
    }

    public void mettreAJourEtat(String ville, List<Vente> toutesVentes) {
        // 1. Récupérer les cartes à vendre depuis cette ville
        for (Vente vente : toutesVentes) {
            if (vente.getVilleVendeur().equals(ville) && !ventesEffectuees.contains(vente)) {
                ventesEffectuees.add(vente);
                cartesEnTransit.add(vente.getVilleAcheteur());
            }
        }

        // 2. Livrer les cartes si on est dans la ville de destination
        cartesEnTransit.remove(ville);
    }

    // Getters essentiels
    public List<String> getItineraire() {
        return itineraire;
    }

    public Set<Vente> getVentesEffectuees() {
        return ventesEffectuees;
    }

    public Set<String> getCartesEnTransit() {
        return cartesEnTransit;
    }
}