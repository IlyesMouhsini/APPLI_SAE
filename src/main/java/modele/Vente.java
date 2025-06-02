package modele;

public class Vente {
    private Membre vendeur;
    private Membre acheteur;

    public Vente(Membre vendeur, Membre acheteur) {
        this.vendeur = vendeur;
        this.acheteur = acheteur;
    }

    public Membre getVendeur() {
        return vendeur;
    }

    public Membre getAcheteur() {
        return acheteur;
    }

    @Override
    public String toString() {
        return vendeur.getPseudo() + " (" + vendeur.getVille() + ")"
                + " -> "
                + acheteur.getPseudo() + " (" + acheteur.getVille() + ")";
    }

    // Nouvelles méthodes pour compatibilité avec GrapheOriente et SolutionPartielle
    public String getVilleVendeur() {
        return vendeur.getVille();
    }

    public String getVilleAcheteur() {
        return acheteur.getVille();
    }

}
