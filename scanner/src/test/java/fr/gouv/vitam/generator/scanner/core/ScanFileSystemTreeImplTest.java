package fr.gouv.vitam.generator.scanner.core;

import org.junit.Test;

public class ScanFileSystemTreeImplTest {

    @Test
    public void scan() {
        ClassLoader classLoader = getClass().getClassLoader();
        String basedir = classLoader.getResource("sip1").getFile();
        ScanFileSystemTreeImpl s = new ScanFileSystemTreeImpl(basedir,basedir+".json","output.xml");
        try {
            s.scan();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

}
