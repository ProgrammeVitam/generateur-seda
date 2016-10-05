/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 *
 * contact.vitam@culture.gouv.fr
 * 
 * This software is a computer program whose purpose is to implement a digital 
 * archiving back-office system managing high volumetry securely and efficiently.
 *
 * This software is governed by the CeCILL 2.1 license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL 2.1
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
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
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL 2.1 license and that you accept its terms.
 */
package fr.gouv.vitam.generator.dag.main;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.common.PropertiesUtils;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferConfig;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferGenerator;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaMissingFieldException;

/**
 * 
 */
public class DAGGenerator {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(DAGGenerator.class);
    
    private DAGGenerator(){}
    
    /**
     * @param args
     * @throws IOException 
     * @throws XMLStreamException 
     * @throws VitamSedaException 
     */
    public static void main(String[] args) throws IOException, VitamSedaException, XMLStreamException{
        if (args.length < 3){
            System.exit(1);
        }            
        String workingDir = args[0];
        String currentDate = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
        String outputFile = workingDir+"/SIP-"+currentDate+".zip";
        ArchiveTransferConfig atc = new ArchiveTransferConfig(workingDir+"/conf", null);
        try{
            Properties properties = PropertiesUtils.readProperties(PropertiesUtils.findFile("generator.properties"));
            outputFile = properties.getProperty("outputFile",outputFile);
        }catch(FileNotFoundException e){ // NOSONAR : It is OK not to have generator.properties file
            LOGGER.debug("generator.properties is missing . Use default values");
        }
        try{
            generateDag(atc,outputFile,new Integer(args[1]).intValue(),new Integer(args[2]).intValue());
        }catch (VitamSedaMissingFieldException e){ // NOSONAR : global catch of this exception
            System.err.println("Champ Manquant :" +  e.getMessage()); // NOSONAR : 
            System.exit(3);
        }
    }

    /**
     * Launch the scan of directories to create the seda archive unit transfer
     * @param archiveTransferConfig
     * @param outputFile
     * @param levels : number of the levels in the DAG
     * @param levelLength : size of the level in the DAG
     * @throws VitamSedaException 
     * @throws XMLStreamException
     */
    public static void generateDag(ArchiveTransferConfig archiveTransferConfig,String outputFile, int levels, int levelLength) throws VitamSedaException, XMLStreamException{
        String[] sortedNodes = new String [levels*levelLength];
        ArchiveTransferGenerator atg = new ArchiveTransferGenerator(archiveTransferConfig, outputFile);
        atg.generateHeader();
        // Generate the node of the DAG
        for (int i = 0;i < levels;i++){
            for (int j=0;j < levelLength;j++){
                String message = "Archive Unit ("+ i +"_" + j+")";
                sortedNodes[i*levelLength + j ] = atg.addArchiveUnit(message, message);
            }
        }
        // Create an edge between all node which are inferior on the topological order
        // The first level are independant roots
        for (int i=levels;i< (levels*levelLength);i++){
            for (int j=i+1;j < (levels*levelLength);j++){
                atg.addArchiveUnit2ArchiveUnitReference(sortedNodes[i],sortedNodes[j]);
            }
        }
        atg.writeDescriptiveMetadata();
        atg.writeManagementMetadata();
        atg.closeDocument();
    }

}
