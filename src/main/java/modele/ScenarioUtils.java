package modele;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class ScenarioUtils {

    public static List<String> listerScenarios() {
        String basePath = System.getProperty("user.dir");
        File dossier = new File(basePath + "/scenarios/");
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
