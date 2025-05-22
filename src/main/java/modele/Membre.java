package modele;

public class Membre {
    private String nom;
    private String ville;

    public Membre(String nom, String ville) {
        this.nom = nom;
        this.ville = ville;
    }

    public String getNom() { return nom; }
    public String getVille() { return ville; }

    @Override
    public String toString() {
        return nom + " (" + ville + ")";
    }
}
