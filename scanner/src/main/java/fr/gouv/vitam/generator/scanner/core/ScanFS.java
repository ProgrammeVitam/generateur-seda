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
package fr.gouv.vitam.generator.scanner.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;

import fr.gouv.vitam.common.CharsetUtils;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.core.Playbook;
import fr.gouv.vitam.generator.scheduler.core.PlaybookBuilder;
import fr.gouv.vitam.generator.scheduler.core.SchedulerEngine;
import fr.gouv.vitam.generator.scheduler.exception.VitamSchedulerException;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferConfig;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferGenerator;
import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;

/**
 * Scanner a FileSystem
 */
public class ScanFS extends SimpleFileVisitor<Path> implements AutoCloseable {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ScanFS.class);
    // TODO Défini plusieurs fois => un common ?
    private static final String MANIFEST_NAME = "manifest.json";
    private static final String IGNORE_PATTERNS_JSON_KEY = "ignore_patterns";
    private final ArchiveTransferGenerator atgi;
    // null when the current directory is an ArchiveUnit and id of the current DataobjectGroup if the directory is an
    // DataobjectGroup
    private String dataObjectGroupOfCurrentDirectory;
    private final HashMap<String, String> mapArchiveUnitPath2Id;
    private final SchedulerEngine schedulerEngine;
    private final Playbook playbookBinary;
    private final PrintStream errFileStream;
    private final Set<PathMatcher> excludeFileSet = new HashSet<>();
    private final ArchiveTransferConfig archiveTransferConfig;
    private final long beginTimeMS;
    private int numberBinaryDataObject=0;
    /**
     * Constructor for ScanFS
     * @param archiveTransferConfig : contains the aggregate configuration of the differents configurations sources
     * @param playbookFileBDO : Path of the file which contains the Playbook for Binary Data Object
     * @param outputFile : Path of the ZIP Seda File
     * @param errFile : Path of the error/rejected File
     * @throws VitamException
     */
    public ScanFS(ArchiveTransferConfig archiveTransferConfig, String playbookFileBDO,String outputFile,String errFile) throws VitamException {
        super();
        ParametersChecker.checkParameter("ConfigObject cannot be null", archiveTransferConfig);
        ParametersChecker.checkParameter("playbookBinaryFile cannot be null", playbookFileBDO);
        ParametersChecker.checkParameter("outputFile cannot be null", outputFile);
        ParametersChecker.checkParameter("errFile cannot be null", errFile);
        this.atgi = new ArchiveTransferGenerator(archiveTransferConfig,outputFile);
        this.schedulerEngine = new SchedulerEngine();
        this.playbookBinary = PlaybookBuilder.getPlaybook(playbookFileBDO);
        this.archiveTransferConfig = archiveTransferConfig;
        try {
            this.errFileStream = new PrintStream(errFile, CharsetUtils.UTF_8);
        }catch (UnsupportedEncodingException e){
            throw new VitamException(CharsetUtils.UTF_8+" is not a valid Charset",e);
        }catch (FileNotFoundException e){
            throw new VitamException("Can't write the error file",e);
        }
        try {
            atgi.generateHeader();
        } catch (XMLStreamException e) {
            throw new VitamException("Exception lors de la génération du header XML", e);
        }
        mapArchiveUnitPath2Id = new HashMap<>();
        setExcludedFileList();
        beginTimeMS = System.currentTimeMillis();
    }
    
    private void setExcludedFileList(){
        if (archiveTransferConfig.has(IGNORE_PATTERNS_JSON_KEY) && archiveTransferConfig.get(IGNORE_PATTERNS_JSON_KEY).isArray()){
            ArrayNode ignorePatterns = (ArrayNode) archiveTransferConfig.get(IGNORE_PATTERNS_JSON_KEY);
            Iterator<JsonNode> itr = ignorePatterns.elements();
            while(itr.hasNext()){
                excludeFileSet.add(FileSystems.getDefault().getPathMatcher("glob:**/"+itr.next().textValue()));//NOSONAR : The default FileSystem must not be closed : https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close%28%29
            }
        }          
    }
    
    /**
     * Action that occurs when we enter in a directory : - if the directory begins and ends with "__" it is an
     * DataObjectGroup . A virtual ArchiveUnit is created as the father of the DataObjectGroup This directory MUSTN'T
     * have subdirectories - else the directory represents an ArchiveUnit
     */

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String dirName = dir.getFileName().toString();
        File manifestPathName = null;
        if (new File(dir.toFile().toString()+dir.getFileSystem().getSeparator()+MANIFEST_NAME).isFile()){
            manifestPathName = new File(dir.toFile().toString()+dir.getFileSystem().getSeparator()+MANIFEST_NAME);
            LOGGER.debug(manifestPathName.toString());
        }        
        dataObjectGroupOfCurrentDirectory = null;
        // DataObjectGroup : The directory is a DataObjectGroup so we create a pseudo ArchiveUnitID
        if (dirName.startsWith("__") && dirName.endsWith("__")) {
            dataObjectGroupOfCurrentDirectory = atgi.getDataObjectGroupUsedMap().registerDataObjectGroup();
            String archiveUnitID = atgi.addArchiveUnit(dir.getFileName().toString(), dir.toString(),manifestPathName);
            mapArchiveUnitPath2Id.put(dir.toString(),archiveUnitID);
            String fatherID = mapArchiveUnitPath2Id.get(dir.getParent().toString());
            atgi.addArchiveUnit2ArchiveUnitReference(fatherID, archiveUnitID);
            atgi.addArchiveUnit2DataObjectGroupReference(archiveUnitID, dataObjectGroupOfCurrentDirectory);
        // ArchiveUnit
        } else {
            String id = atgi.addArchiveUnit(dirName, dir.toString(),manifestPathName);
            mapArchiveUnitPath2Id.put(dir.toString(), id);
            if (mapArchiveUnitPath2Id.containsKey(dir.getParent().toString())) {
                String fatherID = mapArchiveUnitPath2Id.get(dir.getParent().toString());
                atgi.addArchiveUnit2ArchiveUnitReference(fatherID, id);
            }
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Actions that occured when we reach a file . 2 options : - The father directory is a DataObjectGroup . The file is
     * a BinaryDataObject that will be added to the DataObjectGroup - The father directory is a ArchiveUnit . We create
     * a pseudo Archive Unit and a DataObjectGroup where will be attached the BinaryObjectGroup
     */
    @Override
    public FileVisitResult visitFile(Path file,
        BasicFileAttributes attr) {
        if (file.getFileName().toString().equals(ArchiveTransferConfig.CONFIG_NAME) || file.getFileName().toString().equals(MANIFEST_NAME)){
            return FileVisitResult.CONTINUE;
        }
        for(PathMatcher pm : excludeFileSet){
            if (pm.matches(file)){
                errFileStream.println("The file :"+ file + " has been rejected as matching a pattern in ArchiveTransferConfig.json file");
                return FileVisitResult.CONTINUE;
            }
        }

        // If the file is not a regular file, ignore
        if (!(attr.isSymbolicLink() || attr.isRegularFile())) {
            return FileVisitResult.CONTINUE;
        }
        String dataObjectGroupID;
        String archiveUnitID;
        String fatherID;
        
        // Archive Unit : we create the DataObjectGroup
        if (dataObjectGroupOfCurrentDirectory == null){
            dataObjectGroupID = atgi.getDataObjectGroupUsedMap().registerDataObjectGroup();
        } else {
            // The directory is a DataObjectGroup : the DataObjectGroup ID is known (has been defined previously
            // when we manage the father directory)
            dataObjectGroupID = dataObjectGroupOfCurrentDirectory;
        }
        // Prepare the parameters
        ParameterMap inputParameterMap=new ParameterMap();
        inputParameterMap.put("file",file.toUri().getPath());
        inputParameterMap.put("dataobjectgroupID",dataObjectGroupID);
        inputParameterMap.put("archivetransfergenerator", atgi);
        try {
            schedulerEngine.execute(playbookBinary, inputParameterMap);
            // Standard Directory
            if (dataObjectGroupOfCurrentDirectory == null) {
                // Create the Pseudo ArchiveUnit
                archiveUnitID = atgi.addArchiveUnit(file.getFileName().toString(),
                    "Pseudo Archive Unit du fichier :" + file.toString());
                // Get the ID of the parent ArchiveUnit
                fatherID = mapArchiveUnitPath2Id.get(file.getParent().toString());
                // Add the relation between father and son
                atgi.addArchiveUnit2ArchiveUnitReference(fatherID, archiveUnitID);
                // Add the relation between son AU and DataObjectGroup
                atgi.addArchiveUnit2DataObjectGroupReference(archiveUnitID, dataObjectGroupID);
                // Calculate TransactedDate,StartDate and EndDate
                atgi.setTransactedDate(archiveUnitID, new Date(file.toFile().lastModified()));
            // DataObjectGroup Directory
            }else{
                // Get the ID of the DataObjectGroup Directory Archive Unit 
                fatherID = mapArchiveUnitPath2Id.get(file.getParent().toString());
                // If the file is the BinaryMaster Version, set the Transacted, StartDate and EndDate in the Archive Unit
                if (file.getFileName().toString().startsWith("BinaryMaster")){
                    atgi.setTransactedDate(fatherID, new Date(file.toFile().lastModified()));
                }
            }
            numberBinaryDataObject++;
        } catch (VitamBinaryDataObjectException e){//NOSONAR : This exception is for BinaryDataObject rejected file
            LOGGER.warn(file.toUri().getPath() + " has been rejected for the reason : "+ e.getMessage());
            errFileStream.println(e.getMessage());
        } catch (VitamSchedulerException|VitamSedaException e) {
            LOGGER.error(e);
        } catch (VitamException e){
            LOGGER.error(e);
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Action that occurs when we leave the directory Nothing is done in our implementation
     */

    @Override
    public FileVisitResult postVisitDirectory(Path dir,
        IOException exc) {
        String dirName = dir.getFileName().toString();
        // When in the ArchiveUnit Standard mode (not DOG), the start and endDate have to be calculated recursively
        if (dataObjectGroupOfCurrentDirectory == null ) {           
             String auid = mapArchiveUnitPath2Id.get(dir.toString());
             atgi.addStartAndEndDate2ArchiveUnit(auid);
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Catch the possible IOException so that the scan is not stopped on an IOException
     */
    @Override
    public FileVisitResult visitFileFailed(Path file,IOException e) {
        LOGGER.error("Error on reading " + file + " : " + e.getMessage());
        return FileVisitResult.CONTINUE;
    }
    
    /**
     * At the end of the scan we write the descriptive and Management metadata (the ArchiveUnit are in memory) and flush
     * all the buffers to close correctly the XML
     */
    @Override
    public void close() throws XMLStreamException, VitamSedaException {
        long binaryDataObjecttotalTime = (System.currentTimeMillis()-beginTimeMS);
        LOGGER.info("Managing BinaryDataObjects : "+ binaryDataObjecttotalTime + " ms for "+ numberBinaryDataObject +" BinaryDataObjects (time per BDO : "+ binaryDataObjecttotalTime/numberBinaryDataObject +" ms)");
        long beginDescriptiveMetadateTime = System.currentTimeMillis();
        int nbArchiveUnits = atgi.writeDescriptiveMetadata();
        long descriptiveMetadataTotalTime = (System.currentTimeMillis()-beginDescriptiveMetadateTime);
        LOGGER.info("Writing ArchiveUnits : "+ descriptiveMetadataTotalTime + " ms for "+ nbArchiveUnits + " ArchiveUnits (time per AU : " + descriptiveMetadataTotalTime/nbArchiveUnits+" ms)");
        atgi.writeManagementMetadata();
        atgi.closeDocument();
        schedulerEngine.printStatistics();
    }
}
