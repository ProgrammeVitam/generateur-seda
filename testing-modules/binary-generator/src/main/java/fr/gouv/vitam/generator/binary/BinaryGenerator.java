/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital
 * archiving back-office system managing high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL 2.1
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL 2.1 license and that you accept its terms.
 */
package fr.gouv.vitam.generator.binary;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.core.Playbook;
import fr.gouv.vitam.generator.scheduler.core.PlaybookBuilder;
import fr.gouv.vitam.generator.scheduler.core.SchedulerEngine;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferConfig;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferGenerator;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaMissingFieldException;

/**
 * Main class to launch a complete SIP from scratch, with everything generated.
 *
 * Main arguments : fileNumber fileSize [outputFile]
 */
public class BinaryGenerator {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(BinaryGenerator.class);

    /**
     * Digit to text representation
     */
    private static final String[] DIGIT_NAMES =
        {"zero", "one", "two", "three", "four", "five", "six", "seven", "eight", "nine"};


    private BinaryGenerator() {
    }

    /**
     * @param args
     * @throws IOException
     * @throws XMLStreamException
     * @throws VitamSedaException
     */
    public static void main(String[] args) throws IOException, VitamSedaException, XMLStreamException {
        final Path outputFile;
        final int fileNumber;
        final long fileSize;
        String workingDir = args[0];
        if (args.length < 3) {
            fileNumber = -1;
            fileSize = -1;
            outputFile = null;
            System.err.println(
                "Arguments attendus : <nombre de binaryObject> <taille d'un binaryObject (octets)> [<chemin du fichier à générer>]");
            System.exit(1);
        } else if (args.length < 4) {
            fileNumber = Integer.parseInt(args[1]);
            fileSize = Long.parseLong(args[2]);
            outputFile = Paths.get(workingDir, "SIP-" + fileNumber + "-" + fileSize + ".zip");
        } else {
            fileNumber = Integer.parseInt(args[1]);
            fileSize = Long.parseLong(args[2]);
            outputFile = Paths.get(args[3]);
        }

        Path playbookFileBDO = Paths.get(workingDir, "conf", "playbook_GeneratedBinaryDataObject.json");

        ArchiveTransferConfig atc = new ArchiveTransferConfig(workingDir + "/conf", null);
        atc.setMessageIdentifer("BinaryGenerator " + fileNumber + " - " + fileSize);
        try {
            generateBinary(atc, playbookFileBDO, outputFile, fileNumber, fileSize);
        } catch (VitamSedaMissingFieldException e) { // NOSONAR : global catch of this exception
            System.err.println("Champ manquant :" + e.getMessage()); // NOSONAR :
            System.exit(3);
        } catch (VitamException e) {// NOSONAR : don't rethrow as we exit
            System.err.println("Erreur générale :" + e.getMessage()); // NOSONAR :
            System.exit(3);
        }
    }

    /**
     * Generate a SIP with fully generated content.
     *
     * @param archiveTransferConfig : ATC configuration
     * @param playbookFileBDO : Path of the playbook that will be used to manage BinaryDataObject
     * @param outputFile : path of the file to generate
     * @param fileNumber : number of files to include inside this SIP
     * @param fileSize : size of the file that must be included
     * @throws VitamSedaException
     * @throws XMLStreamException
     * @throws VitamException
     */
    public static void generateBinary(ArchiveTransferConfig archiveTransferConfig, Path playbookFileBDO,
        Path outputFile, int fileNumber, long fileSize) throws VitamException, XMLStreamException {
        SchedulerEngine schedulerEngine = new SchedulerEngine();
        Playbook playbookBinary = PlaybookBuilder.getPlaybook(playbookFileBDO.toString());
        // Initialize atg
        ArchiveTransferGenerator atg = new ArchiveTransferGenerator(archiveTransferConfig, outputFile.toString());
        atg.generateHeader();
        // Generate root AU
        String messageRoot = "Archive Unit (root)";
        final String rootAuId = atg.addArchiveUnit(messageRoot, messageRoot);
        // Generate the nodes & files
        LOGGER.info("Generating a SIP with {} files...", fileNumber);
        ProgressBar bar = new ProgressBar();
        for (int i = 0; i < fileNumber; i++) {
            bar.update(i, fileNumber);
            // Generate AU + link with rootAU
            final String auId = atg.addArchiveUnit("Generated Archive Unit (" + i + ")", "Generated Archive Unit " + spellNumber(i));
            atg.addArchiveUnit2ArchiveUnitReference(rootAuId, auId);
            // Generate GOT + link with AU
            String dataObjectGroupID = atg.getDataObjectGroupUsedMap().registerDataObjectGroup();
            atg.addArchiveUnit2DataObjectGroupReference(auId, dataObjectGroupID);
            // Prepare the parameters
            ParameterMap inputParameterMap = new ParameterMap();
            inputParameterMap.put("file", "file-" + i + ".txt");
            inputParameterMap.put("size", fileSize);
            inputParameterMap.put("dataobjectgroupID", dataObjectGroupID);
            inputParameterMap.put("archivetransfergenerator", atg);
            schedulerEngine.execute(playbookBinary, inputParameterMap);
            atg.setTransactedDate(auId, new Date());
        }
        // Write SIP trailer
        atg.writeDescriptiveMetadata();
        atg.writeManagementMetadata();
        atg.closeDocument();
        // End
        LOGGER.info("SIP generation done with success ! Result is stored in the file {}", outputFile);
        schedulerEngine.printStatistics();
    }


    /**
     * Convert an int into a literal string representation ; e.g. : 102 --> one zero two
     *
     * @param number
     * @return
     */
    private static String spellNumber(int number) {
        int remainingNumber = number;
        StringBuilder sb = new StringBuilder();
        while (remainingNumber >= 10) {
            sb.append(DIGIT_NAMES[remainingNumber % 10]).append(' ');
            remainingNumber = remainingNumber / 10;
        }
        sb.append(DIGIT_NAMES[remainingNumber]);
        return sb.toString();

    }


}
