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
package fr.gouv.vitam.generator.scanner.core;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;

import javax.xml.stream.XMLStreamException;

// FIXME : factory
import fr.gouv.vitam.generator.seda.core.ArchiveTransferGeneratorImpl;

/**
 * 
 */
class ScanFS extends SimpleFileVisitor<Path> {
    
    private static final String MANIFEST_NAME = "manifest.json";
    private ArchiveTransferGeneratorImpl  atgi ;
    private URI baseURI;
    // null when the current directory is an ArchiveUnit and id of the current DataobjectGroup if the directory is an DataobjectGroup 
    private String currentDataObjectGroup;
    private HashMap<String,String> mapArchiveUnitPath2Id; 
    
    public ScanFS(Path basePath,String configFile,String outputFile) throws XMLStreamException{
        super();
        this.baseURI = basePath.toUri();
        this.atgi = new ArchiveTransferGeneratorImpl(outputFile);
        atgi.generateHeader(configFile);
        mapArchiveUnitPath2Id = new HashMap<>();
    }
    
    @Override
    public FileVisitResult preVisitDirectory(Path dir,BasicFileAttributes attrs){
        String dirName = dir.getFileName().toString();
        currentDataObjectGroup = null;
        // DataObjectGroup
        if (dirName.startsWith("__") && dirName.endsWith("__")){
           currentDataObjectGroup = atgi.registerDataObjectGroup();
           if (mapArchiveUnitPath2Id.containsKey(dir.getParent().toString())){
               String fatherID = mapArchiveUnitPath2Id.get(dir.getParent().toString());
               atgi.addArchiveUnit2DataObjectGroupReference(fatherID, currentDataObjectGroup);
           }
        // ArchiveUnit
        }else{
           String id = atgi.addArchiveUnit(dirName, dir.toString());
           mapArchiveUnitPath2Id.put(dir.toString(),id);
           if (mapArchiveUnitPath2Id.containsKey(dir.getParent().toString())){
               String fatherID = mapArchiveUnitPath2Id.get(dir.getParent().toString());
               atgi.addArchiveUnit2ArchiveUnitReference(fatherID, id);
           }
        }
        return FileVisitResult.CONTINUE;
    }
    
    @Override
    public FileVisitResult visitFile(Path file,
                                   BasicFileAttributes attr){
        // Presence of a manifest.json file (not currently implemented)
        String dataObjectGroupID;
        if ((attr.isSymbolicLink() || attr.isRegularFile()) && !file.getFileName().toString().equals(MANIFEST_NAME)) {
            try{
                // Archive Unit : we create one archive unit (and one DataObjectGroup per File)
                if (currentDataObjectGroup == null ){
                    String archiveUnitID = atgi.addArchiveUnit(file.getFileName().toString(),"Pseudo Archive Unit du fichier :" + file.toString());
                     dataObjectGroupID = atgi.registerDataObjectGroup();
                    String fatherID = mapArchiveUnitPath2Id.get(file.getParent().toString());
                    atgi.addArchiveUnit2ArchiveUnitReference(fatherID, archiveUnitID);
                    atgi.addArchiveUnit2DataObjectGroupReference(archiveUnitID,dataObjectGroupID);
                // DataObjectGroup  : the DataObjectGroup ID is known 
                }else{
                    dataObjectGroupID = currentDataObjectGroup;
                }
                atgi.addBinaryDataObject("/"+baseURI.relativize(file.toUri()).getPath(),file.toString(),dataObjectGroupID);
            }catch(XMLStreamException e){
                e.printStackTrace();
            }
        } 
        return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult postVisitDirectory(Path dir,
                                          IOException exc) {
        return FileVisitResult.CONTINUE;
    }
    
    public void endScan() throws XMLStreamException{
        atgi.closeDocument();
    }
} 
