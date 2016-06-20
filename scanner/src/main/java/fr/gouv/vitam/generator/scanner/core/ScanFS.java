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

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Date;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.core.Playbook;
import fr.gouv.vitam.generator.scheduler.core.PlaybookBuilder;
import fr.gouv.vitam.generator.scheduler.core.SchedulerEngine;
import fr.gouv.vitam.generator.scheduler.exception.VitamSchedulerException;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferGenerator;
import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;

/**
 * TODO
 */
class ScanFS extends SimpleFileVisitor<Path> {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ScanFS.class);
    // TODO Défini plusieurs fois => un common ?
    private static final String MANIFEST_NAME = "manifest.json";

    // TODO final
    private ArchiveTransferGenerator atgi;
    // null when the current directory is an ArchiveUnit and id of the current DataobjectGroup if the directory is an
    // DataobjectGroup
    private String dataObjectGroupOfCurrentDirectory;
    // TODO final
    private HashMap<String, String> mapArchiveUnitPath2Id;
    // TODO final
    private SchedulerEngine schedulerEngine;
    // TODO final
    private Playbook playbookBinary;

    public ScanFS(String configFile, String playbookBinaryFile,String outputFile) throws VitamException {
        super();
        // Null check ?
        this.atgi = new ArchiveTransferGenerator(outputFile);
        this.schedulerEngine = new SchedulerEngine();
        this.playbookBinary = PlaybookBuilder.getPlaybook(playbookBinaryFile);
        try {
            atgi.generateHeader(configFile);
        } catch (XMLStreamException e) {
            throw new VitamException("Exception lors de la génération du header XML", e);
        }
        mapArchiveUnitPath2Id = new HashMap<>();
    }

    /**
     * Action that occurs when we enter in a directory : - if the directory begins and ends with "__" it is an
     * DataObjectGroup . A virtual ArchiveUnit is created as the father of the DataObjectGroup This directory MUSTN'T
     * have subdirectories - else the directory represents an ArchiveUnit
     */

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
        String dirName = dir.getFileName().toString();
        dataObjectGroupOfCurrentDirectory = null;
        // DataObjectGroup : The directory is a DataObjectGroup so we create a pseudo ArchiveUnitID
        if (dirName.startsWith("__") && dirName.endsWith("__")) {
            dataObjectGroupOfCurrentDirectory = atgi.getDataObjectGroupUsedMap().registerDataObjectGroup();
            String archiveUnitID = atgi.addArchiveUnit(dir.getFileName().toString(), dir.toString());
            String fatherID = mapArchiveUnitPath2Id.get(dir.getParent().toString());
            atgi.addArchiveUnit2ArchiveUnitReference(fatherID, archiveUnitID);
            atgi.addArchiveUnit2DataObjectGroupReference(archiveUnitID, dataObjectGroupOfCurrentDirectory);
            // ArchiveUnit
        } else {
            String id = atgi.addArchiveUnit(dirName, dir.toString());
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
        // Presence of a manifest.json file (not currently implemented)
        String dataObjectGroupID;
        String archiveUnitID = "";
        String fatherID = "";
        if ((attr.isSymbolicLink() || attr.isRegularFile()) && !file.getFileName().toString().equals(MANIFEST_NAME)) {
            // Archive Unit : we create the DataObjectGroup
            if (dataObjectGroupOfCurrentDirectory == null){
                dataObjectGroupID = atgi.getDataObjectGroupUsedMap().registerDataObjectGroup();
            } else {
                // The directory is a DataObjectGroup : the DataObjectGroup ID is known (has been defined previously
                // when we manage the father directory)
                dataObjectGroupID = dataObjectGroupOfCurrentDirectory;
            }
            try {
                ParameterMap inputParameterMap=new ParameterMap();
                inputParameterMap.put("file",file.toUri().getPath());
                inputParameterMap.put("dataobjectgroupID",dataObjectGroupID);
                inputParameterMap.put("archivetransfergenerator", atgi);
                schedulerEngine.execute(playbookBinary, inputParameterMap);
                if (dataObjectGroupOfCurrentDirectory == null) {
                    archiveUnitID = atgi.addArchiveUnit(file.getFileName().toString(),
                        "Pseudo Archive Unit du fichier :" + file.toString());
                    fatherID = mapArchiveUnitPath2Id.get(file.getParent().toString());
                    atgi.addArchiveUnit2ArchiveUnitReference(fatherID, archiveUnitID);
                    atgi.addArchiveUnit2DataObjectGroupReference(archiveUnitID, dataObjectGroupID);
                    atgi.setTransactedDate(archiveUnitID, (Date) inputParameterMap.get("file.mtime")); 
                }
            } catch (VitamBinaryDataObjectException e){//NOSONAR : This exception is for BinaryDataObject rejected file
                LOGGER.warn(file.toUri().getPath() + " has been rejected for the reason : "+ e.getMessage());  
            } catch (VitamSchedulerException|VitamSedaException e) {
                LOGGER.error(e);
            } catch (VitamException e){
                LOGGER.error(e);
            }
    
        }
        return FileVisitResult.CONTINUE;
    }

    /**
     * Action that occurs when we leave the directory Nothing is done in our implementation
     */

    @Override
    public FileVisitResult postVisitDirectory(Path dir,
        IOException exc) {
        return FileVisitResult.CONTINUE;
    }

    /**
     * Catch the possible IOException so that the scan is not stopped on an IOException
     */
    @Override
    public FileVisitResult visitFileFailed(Path file,
        IOException e) {
        LOGGER.error("Error on reading " + file + " : " + e.getMessage());
        return FileVisitResult.CONTINUE;
    }

    /**
     * At the end of the scan we write the descriptive and Management metadata (the ArchiveUnit are in memory) and flush
     * all the buffers to close correctly the XML
     * @throws XMLStreamException
     * @throws VitamSedaException
     */
    public void endScan() throws XMLStreamException, VitamSedaException {
        atgi.writeDescriptiveMetadata();
        atgi.writeManagementMetadata();
        atgi.closeDocument();
        schedulerEngine.printStatistics();
    }
}
