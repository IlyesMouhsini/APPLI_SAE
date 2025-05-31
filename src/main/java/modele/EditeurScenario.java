package modele;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class EditeurScenario {

    private Scenario scenario;

    public EditeurScenario(Scenario scenario) {
        this.scenario = scenario;
    }

    public void ajouterVente(Membre vendeur, Membre acheteur) {
        scenario.getVentes().add(new Vente(vendeur, acheteur));
    }

    public void supprimerVente(int index) {
        if (index >= 0 && index < scenario.getVentes().size()) {
            scenario.getVentes().remove(index);
        }
    }

    public void modifierVente(int index, Membre nouveauVendeur, Membre nouvelAcheteur) {
        if (index >= 0 && index < scenario.getVentes().size()) {
            scenario.getVentes().set(index, new Vente(nouveauVendeur, nouvelAcheteur));
        }
    }

    public void sauvegarder(String nomFichier) throws IOException {
        String basePath = System.getProperty("user.dir");
        Path chemin = Paths.get(System.getProperty("user.dir"), "scenarios", nomFichier);
        try (BufferedWriter writer = Files.newBufferedWriter(chemin)) {
            for (Vente v : scenario.getVentes()) {
                writer.write(v.getVendeur().getPseudo() + " -> " + v.getAcheteur().getPseudo());
                writer.newLine();
            }
        }
    }

    public Scenario getScenario() {
        return scenario;
    }
}
