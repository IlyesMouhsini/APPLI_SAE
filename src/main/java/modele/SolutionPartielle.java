package modele;

import java.util.*;

public class SolutionPartielle {
    private List<String> itineraire;
    private Set<Vente> ventesEffectuees;
    private Set<String> cartesEnTransit;
    private double distanceParcourue;

    public SolutionPartielle() {
        this.itineraire = new ArrayList<>();
        this.ventesEffectuees = new HashSet<>();
        this.cartesEnTransit = new HashSet<>();
        this.distanceParcourue = 0;
    }

    public void ajouterVille(String ville) {
        itineraire.add(ville);
    }

    public boolean estComplete(List<Vente> toutesVentes) {
        return ventesEffectuees.size() == toutesVentes.size() && cartesEnTransit.isEmpty();
    }

    public Set<String> getVillesPossibles(List<Vente> ventes) {
        Set<String> possibles = new HashSet<>();

        // Villes où on peut livrer
        possibles.addAll(cartesEnTransit);

        // Villes où on peut prendre des cartes
        for (Vente vente : ventes) {
            if (!ventesEffectuees.contains(vente)) {
                possibles.add(vente.getVilleVendeur());
            }
        }

        return possibles;
    }

    public double getDistanceEstimee() {
        return distanceParcourue; // Simplification - on pourrait ajouter une heuristique
    }

    public SolutionPartielle copier() {
        SolutionPartielle copie = new SolutionPartielle();
        copie.itineraire = new ArrayList<>(this.itineraire);
        copie.ventesEffectuees = new HashSet<>(this.ventesEffectuees);
        copie.cartesEnTransit = new HashSet<>(this.cartesEnTransit);
        copie.distanceParcourue = this.distanceParcourue;
        return copie;
    }

    // Getters et setters
    public List<String> getItineraire() {
        return itineraire;
    }

    public Set<Vente> getVentesEffectuees() {
        return ventesEffectuees;
    }

    public Set<String> getCartesEnTransit() {
        return cartesEnTransit;
    }

    public double getDistanceParcourue() {
        return distanceParcourue;
    }

    public void setDistanceParcourue(double distance) {
        this.distanceParcourue = distance;
    }

    public void ajouterVenteEffectuee(Vente vente) {
        ventesEffectuees.add(vente);
    }

    public void ajouterCarteEnTransit(String ville) {
        cartesEnTransit.add(ville);
    }

    public void retirerCarteEnTransit(String ville) {
        cartesEnTransit.remove(ville);
    }

    public void mettreAJourEtat(String ville, List<Vente> toutesVentes) {
        // Récupérer les cartes à vendre depuis cette ville
        for (Vente vente : toutesVentes) {
            if (vente.getVilleVendeur().equals(ville) && !ventesEffectuees.contains(vente)) {
                ventesEffectuees.add(vente);
                cartesEnTransit.add(vente.getVilleAcheteur());
            }
        }

        // Livrer les cartes si on est dans la ville de destination
        cartesEnTransit.remove(ville);
    }
}