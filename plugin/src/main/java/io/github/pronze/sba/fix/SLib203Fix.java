package io.github.pronze.sba.fix;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.FilenameFilter;

import org.bukkit.enchantments.Enchantment;

import io.github.pronze.sba.SBA;
import io.github.pronze.sba.config.SBAConfig;

public class SLib203Fix extends BaseFix {

    @Override
    public void detect() {

    }

    public void shopReplace(String search, String replace) {
        var path2 = SBA.getBedwarsPlugin().getDataFolder().listFiles();

        for (File f : path2) {
            if (f.getName().toLowerCase().endsWith("yml")) {
                try {
                    FileReader fr = new FileReader(f);
                    String s;
                    String totalStr = "";
                    try (BufferedReader br = new BufferedReader(fr)) {

                        while ((s = br.readLine()) != null) {
                            totalStr += s + System.lineSeparator();
                        }
                        totalStr = totalStr.replace(search, replace);
                        FileWriter fw = new FileWriter(f);
                        fw.write(totalStr);
                        fw.close();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void fix(SBAConfig cfg) {
        shopReplace("FIREWORK;", "FIREWORK_ROCKET;");
        shopReplace("KNOCKBACK:", "knockback:");
        shopReplace("ARROW_DAMAGE:", "power:");
        shopReplace("ARROW_KNOCKBACK:", "punch:");
        shopReplace(": JUMP", ": jump_boost");
        shopReplace(": REGENERATION", ": regeneration");
        shopReplace(": SLOW", ": slowness");
        shopReplace(": FAST_DIGGING", ": haste");
        shopReplace(": SLOW_DIGGING", ": mining_fatigue");
        shopReplace(": INCREASE_DAMAGE", ": strength");
        shopReplace(": HEAL", ": instant_health");
        shopReplace(": HARM", ": instant_damage");
        shopReplace(": CONFUSION", ": nausea");
        shopReplace(": DAMAGE_RESISTANCE", ": resistance");

        try{
            for (Enchantment values : Enchantment.values()) {
                shopReplace(values.getName()+":", values.getKey().getKey()+":");
            }
        }
        catch(Throwable t)
        {
            //Enchantment enum for removed?
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
