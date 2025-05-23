package modele;

public class Membre {
    private String pseudo;
    private String ville;

    public Membre(String pseudo, String ville) {
        this.pseudo = pseudo;
        this.ville = ville;
    }

    public String getPseudo() { return pseudo; }
    public String getVille() { return ville; }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Membre)) return false;
        Membre m = (Membre) o;
        return pseudo.equals(m.pseudo);
    }

    @Override
    public int hashCode() {
        return pseudo.hashCode();
    }

    @Override
    public String toString() {
        return pseudo + " (" + ville + ")";
    }
}
