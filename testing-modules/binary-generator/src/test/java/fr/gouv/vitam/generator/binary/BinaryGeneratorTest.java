package fr.gouv.vitam.generator.binary;

import org.junit.Rule;
import org.junit.Test;
import org.junit.contrib.java.lang.system.ExpectedSystemExit;

/**
 * Created by kw on 03/02/2017.
 */
public class BinaryGeneratorTest {

    @Rule
    public final ExpectedSystemExit exit = ExpectedSystemExit.none();

    /**
     * Test generation of file with small content (typically smaller than the file name)
     *
     * @throws Exception
     */
    @Test
    public void TestSmallBinaryGeneration() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String workingdir = classLoader.getResource("nominal").getFile();
        String[] args = {workingdir, "10", "2"};
        //exit.expectSystemExitWithStatus(3);
        BinaryGenerator.main(args);
    }

    /**
     * Test generation of file with small content (typically greater than the file name)
     *
     * @throws Exception
     */
    @Test
    public void TestMediumBinaryGeneration() throws Exception {
        ClassLoader classLoader = getClass().getClassLoader();
        String workingdir = classLoader.getResource("nominal").getFile();
        String[] args = {workingdir, "2", "995"};
        //exit.expectSystemExitWithStatus(3);
        BinaryGenerator.main(args);
    }
}
