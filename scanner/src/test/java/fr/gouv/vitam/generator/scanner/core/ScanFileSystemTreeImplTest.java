package fr.gouv.vitam.generator.scanner.core;

import org.junit.Test;

import fr.gouv.vitam.generator.scanner.main.SedaGenerator;

public class ScanFileSystemTreeImplTest {

    @Test
    public void scan() {
        // TODO : PropertyUtils
        ClassLoader classLoader = getClass().getClassLoader();
        String basedir = classLoader.getResource("sip1").getFile();
        String configFile = basedir+".json";
        String playbookFile = classLoader.getResource("playbook_binary.json").getFile();
        try {
            SedaGenerator.scan(basedir,configFile,playbookFile,"output.zip");
        }catch (Exception e){
            // TODO : Logger
            e.printStackTrace();
        }
    }

}
