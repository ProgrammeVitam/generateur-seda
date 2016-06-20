package fr.gouv.vitam.generator.scanner.core;

import org.junit.Test;

public class ScanFileSystemTreeImplTest {

    @Test
    public void scan() {
        // PropertyUtils
        ClassLoader classLoader = getClass().getClassLoader();
        String basedir = classLoader.getResource("sip1").getFile();
        String configFile = basedir+".json";
        String playbookFile = classLoader.getResource("playbook_binary.json").getFile();
        // Once as AutoCloseable try (allo) [ s.scan();}
        ScanFileSystemTree s = new ScanFileSystemTree(basedir,configFile,playbookFile,"output.zip");
        try {
            s.scan();
        }catch (Exception e){
            // Logger
            e.printStackTrace();
        }
    }

}
