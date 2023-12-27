package io.github.pronze.sba.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;

public class WoolFix extends BaseFix {

    @Override
    public void detect() {

    }

    @Override
    public void fix(SBAConfig cfg) {
        var path2 = SBA.getBedwarsPlugin().getDataFolder().toPath().resolve("shop.yml");
        String search = " WOOL;";
        String replace = " WHITE_WOOL;";

        try {
            FileReader fr = new FileReader(path2.toFile());
            String s;
            String totalStr = "";
            try (BufferedReader br = new BufferedReader(fr)) {

                while ((s = br.readLine()) != null) {
                    totalStr += s + System.lineSeparator();
                }
                totalStr = totalStr.replace(search, replace);
                FileWriter fw = new FileWriter(path2.toFile());
                fw.write(totalStr);
                fw.close();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void warn() {

    }

    @Override
    public boolean IsProblematic() {
        return false;
    }

    @Override
    public boolean IsCritical() {
        return false;
    }

}
