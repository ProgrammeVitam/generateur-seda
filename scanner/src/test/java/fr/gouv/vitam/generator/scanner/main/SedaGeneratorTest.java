package fr.gouv.vitam.generator.scanner.main;

import static org.junit.Assert.fail;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import fr.gouv.vitam.common.exception.VitamException;

public class SedaGeneratorTest {
    
    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    @Test
    public void TestScanNominalCase() {
        ClassLoader classLoader = getClass().getClassLoader();
        String workingdir = classLoader.getResource(".").getFile();
        String scandir = classLoader.getResource("sip1").getFile();
        String [] args = { workingdir, scandir};
        try{
            SedaGenerator.main(args);
        }catch (Exception e){
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void Test1Argument(){
        String [] args = {"only1argument"};
        
        try{
            exit.expectSystemExitWithStatus(1);
            SedaGenerator.main(args);
        }catch(VitamException|IOException|XMLStreamException e){
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void TestScanDirIsAFile (){
        ClassLoader classLoader = getClass().getClassLoader();
        String workingdir = classLoader.getResource(".").getFile();
        String scandir = classLoader.getResource("playbook_binary.json").getFile();
        String [] args = { workingdir, scandir};
        try{
            exit.expectSystemExitWithStatus(2);
            SedaGenerator.main(args);
        }catch(VitamException|IOException|XMLStreamException e){
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void TestMissingFieldInConfigFile (){
        ClassLoader classLoader = getClass().getClassLoader();
        String workingdir = classLoader.getResource("bad_conf").getFile();
        String scandir = classLoader.getResource("sip1").getFile();
        String [] args = { workingdir, scandir};
        try{
            exit.expectSystemExitWithStatus(3);
            SedaGenerator.main(args);
        }catch(VitamException|IOException|XMLStreamException e){
            e.printStackTrace();
            fail();
        }
    }
}
