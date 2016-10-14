package fr.gouv.vitam.generator.dag.main;

import static org.junit.Assert.fail;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.junit.Rule;
import org.junit.Test;

import fr.gouv.vitam.common.exception.VitamException;

public class DAGGeneratorTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();
    
    @Test
    public void nominalCase() {
        ClassLoader classLoader = getClass().getClassLoader();
        String workingdir = classLoader.getResource(".").getFile();
        String [] args = { workingdir, "10","10"};
        try{
            DAGGenerator.main(args);
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
            DAGGenerator.main(args);
        }catch(VitamException|IOException|XMLStreamException e){
            e.printStackTrace();
            fail();
        }
    }
    
    @Test
    public void TestMissingFieldInConfigFile (){
        ClassLoader classLoader = getClass().getClassLoader();
        String workingdir = classLoader.getResource("bad_conf").getFile();
        String [] args = { workingdir, "10","10"};
        try{
            exit.expectSystemExitWithStatus(3);
            DAGGenerator.main(args);
        }catch(VitamException|IOException|XMLStreamException e){
            e.printStackTrace();
            fail();
        }
    }

}
