package modele;

public class Vente {
    private String vendeur;
    private String acheteur;

    public Vente(String vendeur, String acheteur) {
        this.vendeur = vendeur;
        this.acheteur = acheteur;
    }

    public String getVendeur() { return vendeur; }
    public String getAcheteur() { return acheteur; }

    @Override
    public String toString() {
        return vendeur + " -> " + acheteur;
    }
}
