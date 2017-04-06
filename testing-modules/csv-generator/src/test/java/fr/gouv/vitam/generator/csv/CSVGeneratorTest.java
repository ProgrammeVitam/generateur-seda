package fr.gouv.vitam.generator.csv;

import static org.junit.Assert.*;

import org.junit.Test;

public class CSVGeneratorTest {

    @Test
    public void test() throws Exception{
        ClassLoader classLoader = getClass().getClassLoader();
        String[] args = { classLoader.getResource(".").getFile(), classLoader.getResource("input.csv").getFile() };
        CSVGenerator.main(args);
    }

}
