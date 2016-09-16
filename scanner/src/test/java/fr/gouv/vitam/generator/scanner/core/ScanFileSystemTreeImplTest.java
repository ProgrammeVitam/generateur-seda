package fr.gouv.vitam.generator.scanner.core;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.gouv.vitam.generator.scanner.main.SedaGenerator;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferConfig;

public class ScanFileSystemTreeImplTest {

    @Test
    public void scan() {
        // TODO : PropertyUtils
        ClassLoader classLoader = getClass().getClassLoader();
        String basedir = classLoader.getResource("sip1").getFile();
        String configDir = classLoader.getResource("conf").getFile();
        ArchiveTransferConfig atc = new ArchiveTransferConfig("/",configDir );
        String playbookFile = classLoader.getResource("playbook_binary.json").getFile();
        try {
            SedaGenerator.scan(basedir,atc,playbookFile,"output.zip","output.err");
        }catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }

}
