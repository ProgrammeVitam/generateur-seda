/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 * 
 * This software is a computer program whose purpose is to implement a digital archiving back-office system managing
 * high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and abiding by the rules of distribution of free
 * software. You can use, modify and/ or redistribute the software under the terms of the CeCILL 2.1 license as
 * circulated by CEA, CNRS and INRIA at the following URL "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and rights to copy, modify and redistribute granted by the license,
 * users are provided only with a limited warranty and the software's author, the holder of the economic rights, and the
 * successive licensors have only limited liability.
 *
 * In this respect, the user's attention is drawn to the risks associated with loading, using, modifying and/or
 * developing or reproducing the software by the user in light of its specific status of free software, that may mean
 * that it is complicated to manipulate, and that also therefore means that it is reserved for developers and
 * experienced professionals having in-depth computer knowledge. Users are therefore encouraged to load and test the
 * software's suitability as regards their requirements in conditions enabling the security of their systems and/or data
 * to be ensured and, more generally, to use and operate it in the same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had knowledge of the CeCILL 2.1 license and that you
 * accept its terms.
 */
package fr.gouv.vitam.generator.scanner.main;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scanner.core.ScanFS;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferConfig;
import fr.gouv.vitam.generator.seda.exception.VitamSedaMissingFieldException;

/**
 * Entry point of the Seda Generator
 */

public class SedaGenerator {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(SedaGenerator.class);

    private SedaGenerator() {
        // Empty constructor
    }

    /**
     * Entry point
     * @param args
     * @throws IOException
     * @throws XMLStreamException
     * @throws VitamException
     */
    public static void main(String[] args) throws IOException, XMLStreamException, VitamException {
        if (args.length < 2) {
            usage();
            System.exit(1);
        }
        String workingDir = args[0];
        String scanDir = args[1];
        String currentDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String playbookFileBDO = workingDir + "/conf/playbook_BinaryDataObject.json";
        String outputFile = workingDir + "/SIP-" + currentDate + ".zip";
        String errFile = workingDir + "SIP-" + currentDate + ".rejected";
        ArchiveTransferConfig atc = new ArchiveTransferConfig(workingDir + "/conf", scanDir);
        File f = new File(scanDir);
        if (!(f.exists() && f.isDirectory())) {
            System.err
                .println("The given path must be a directory (not a single file): " + scanDir);//NOSONAR : error message
            System.exit(2);
        }
        try {
            Properties properties = PropertiesUtils.readProperties(PropertiesUtils.findFile("generator.properties"));
            playbookFileBDO = properties.getProperty("playbookBinaryDataObject", playbookFileBDO);
            outputFile = properties.getProperty("outputFile", outputFile);
            errFile = properties.getProperty("errFile", errFile);
        } catch (FileNotFoundException e) { // NOSONAR : It is OK not to have generator.properties file
            LOGGER.debug("generator.properties is missing . Use default values");
        }
        long beginTimeMS = System.currentTimeMillis();
        LOGGER.info("Generateur SEDA : Beginning of  scan of directory " + scanDir);
        try {
            scan(scanDir, atc, playbookFileBDO, outputFile, errFile);
        } catch (VitamSedaMissingFieldException e) { // NOSONAR : global catch of this exception
            System.err.println("Champ Manquant :" + e.getMessage()); // NOSONAR :
            System.exit(3);
        }
        LOGGER.info("Generateur SEDA : End of scan of directory " + scanDir + " in " +
            (System.currentTimeMillis() - beginTimeMS) + " ms");
    }

    /**
     * Usage function of the program
     */
    private static void usage() {
        System.err.println("2 arguments are expected");//NOSONAR : usage
        System.err.println("- Current Working Dir "); //NOSONAR : usage
        System.err.println("- Directory that must be scanned"); //NOSONAR : usage
    }

    /**
     * Launch the scan of directories to create the seda archive unit transfer
     * @param scanDir
     * @param archiveTransferConfig
     * @param playbookFileBDO
     * @param outputFile
     * @param errFile
     * @throws IOException
     * @throws XMLStreamException
     * @throws VitamException
     */
    public static void scan(String scanDir, ArchiveTransferConfig archiveTransferConfig, String playbookFileBDO,
        String outputFile, String errFile) throws IOException, XMLStreamException, VitamException {
        try (ScanFS sfs = new ScanFS(archiveTransferConfig, playbookFileBDO, outputFile, errFile)) {
            Path p = FileSystems.getDefault().getPath(
                scanDir);//NOSONAR : The default FileSystem must not be closed : https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close%28%29
            Files.walkFileTree(p.toRealPath(), sfs);
        }
    }
}
