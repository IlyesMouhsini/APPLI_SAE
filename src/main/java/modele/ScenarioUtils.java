package modele;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScenarioUtils {

    public static List<String> listerScenarios() {
        File dossier = new File("src/main/resources/scenarios/");
        List<String> fichiers = new ArrayList<>();

        if (dossier.exists() && dossier.isDirectory()) {
            for (File f : dossier.listFiles()) {
                if (f.isFile() && f.getName().endsWith(".txt")) {
                    fichiers.add(f.getName());
                }
            }
        }

        return fichiers;
    }
}
