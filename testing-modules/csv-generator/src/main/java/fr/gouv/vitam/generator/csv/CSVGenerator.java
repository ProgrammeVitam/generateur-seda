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
package fr.gouv.vitam.generator.csv;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import fr.gouv.culture.archivesdefrance.seda.v2.LevelType;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferConfig;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferGenerator;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaMissingFieldException;

/**
 * Main class of CSV Generator
 *
 */
public class CSVGenerator {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(CSVGenerator.class);

    /**
     * 
     */
    private CSVGenerator() {}

    /**
     * Entry-point of the CSVGenerator
     * 
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {
        if (args.length < 2) {
            System.exit(1);
        }
        String workingDir = args[0];
        String csvFile = args[1];
        String currentDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String outputFile = workingDir + "/SIP-" + currentDate + ".zip";
        ArchiveTransferConfig atc = new ArchiveTransferConfig(workingDir + "/conf", null);
        try {
            generatePlan(atc, outputFile, csvFile);
        } catch (VitamSedaMissingFieldException e) { // NOSONAR : global catch of this exception
            System.err.println("Champ Manquant :" + e.getMessage()); // NOSONAR :
            System.exit(3);
        }

    }


    private static void generatePlan(ArchiveTransferConfig archiveTransferConfig, String outputFile, String csvFile)
        throws VitamSedaException, XMLStreamException {
        ArchiveTransferGenerator atg = new ArchiveTransferGenerator(archiveTransferConfig, outputFile);
        atg.generateHeader();
        String nom, cote, serie, rang, niveau, oaaui;
        Map<String, String> archivalIDtoAUID = new HashMap<String, String>();
        Map<String, String> archivalIDtoFatherSerie = new HashMap<String, String>();
        // Generate the node of the DAG
        try (Reader fr = new FileReader(csvFile);CSVParser parser = new CSVParser(fr, CSVFormat.DEFAULT.withHeader().withDelimiter(';'))) {
            for (final CSVRecord record : parser) {
                if (record.size() < 7){
                    continue;
                }
                nom = record.get(1);
                cote = record.get(3);
                serie = record.get(4);
                niveau = record.get(6);
                oaaui = serie + cote;
                String archiveUnitId = atg.addArchiveUnit(nom, null);
                atg.setOriginatingAgencyArchiveUnitIdentifier(archiveUnitId, oaaui);

                archivalIDtoAUID.put(oaaui, archiveUnitId);
                if ("0".equals(niveau)) {
                    // ROOTs are series
                    atg.setDescriptionLevel(archiveUnitId, LevelType.SERIES);
                } else {
                    // Non ROOT : are sub series and have one father
                    archivalIDtoFatherSerie.put(archiveUnitId, serie);
                    atg.setDescriptionLevel(archiveUnitId, LevelType.SUBSERIES);
                }
            }
        } catch (IOException e) {
            LOGGER.error(e);
        }
        for (Entry<String, String> e : archivalIDtoFatherSerie.entrySet()) {
            String fatherId = archivalIDtoAUID.get(e.getValue());
            String sonId = e.getKey();
            if (fatherId == null) {
                LOGGER.warn("Serie " + e.getValue() + " inconnue");
                atg.removeArchiveUnit(sonId);
                continue;
            }
            atg.addArchiveUnit2ArchiveUnitReference(fatherId, sonId);
        }
        atg.writeDescriptiveMetadata();
        atg.writeManagementMetadata();
        atg.closeDocument();
    }

}
