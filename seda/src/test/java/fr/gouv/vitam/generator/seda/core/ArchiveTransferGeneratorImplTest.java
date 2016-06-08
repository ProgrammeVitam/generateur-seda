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
package fr.gouv.vitam.generator.seda.core;

import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.Date;

import javax.xml.stream.XMLStreamException;

import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.junit.Test;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.core.Playbook;
import fr.gouv.vitam.generator.scheduler.core.PlaybookBuilder;
import fr.gouv.vitam.generator.scheduler.core.SchedulerEngine;
import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;

/**
 * 
 */
public class ArchiveTransferGeneratorImplTest {

    private static final String OUTPUT_FILE = "output.zip";

    @Test
    public void correctSeda() {
        try{

        
        
        ClassLoader classLoader = getClass().getClassLoader();
        String headerPath = classLoader.getResource("sip1.json").getFile();
        ArchiveTransferGenerator atgi = new ArchiveTransferGenerator(OUTPUT_FILE);
        atgi.generateHeader(headerPath);
        atgi.startDataObjectPackage();
        String archiveFatherID = atgi.addArchiveUnit("Titre0", "Description0");
        String archiveSonID1 = atgi.addArchiveUnit("Titre1", "Description1");
        String archiveSonID2 = atgi.addArchiveUnit("Titre1", "Description1");
        atgi.setTransactedDate(archiveSonID1, new Date());
        atgi.removeArchiveUnit(archiveSonID2);
        String dataObjectGroup1ID = atgi.getDataObjectGroupUsedMap().registerDataObjectGroup();	
        atgi = addBinaryDataObject(atgi,headerPath,  dataObjectGroup1ID);
        atgi = addBinaryDataObject(atgi,headerPath,  dataObjectGroup1ID);
        atgi.addArchiveUnit2ArchiveUnitReference(archiveFatherID, archiveSonID1);
        atgi.addArchiveUnit2DataObjectGroupReference(archiveSonID1, dataObjectGroup1ID);
        atgi = addBinaryDataObject(atgi,headerPath, null);
        atgi.writeDescriptiveMetadata();
        atgi.writeManagementMetadata();
        atgi.closeDocument();
        }catch(VitamException|XMLStreamException e){
            e.printStackTrace();
            fail("Should not have an exception");
        }
    }
    
    @Test
    public void emptyFile() {
        try{
        ClassLoader classLoader = getClass().getClassLoader();
        String headerPath = classLoader.getResource("sip1.json").getFile();
        ArchiveTransferGenerator atgi = new ArchiveTransferGenerator(OUTPUT_FILE);
        atgi.generateHeader(headerPath);
        atgi.startDataObjectPackage();
        addBinaryDataObject(atgi, classLoader.getResource("empty").getFile() , null);
        }catch(VitamBinaryDataObjectException e){
            return;
        }catch(VitamException|XMLStreamException e){
            fail("The empty file should raise an VitamBinaryDataObjectException");
        }
        fail("The empty file should raise an exception");
    }
    
    private ArchiveTransferGenerator addBinaryDataObject(ArchiveTransferGenerator atgi,String filename, String dataObjectGroupID) throws VitamException{
        ParameterMap pm = new ParameterMap() ;
        pm.put("file", filename);
        pm.put("dataobjectgroupID", dataObjectGroupID);
        pm.put("archivetransfergenerator", atgi);
        Playbook pb=null;
        ClassLoader classLoader = getClass().getClassLoader();
        String jsonFile = classLoader.getResource("playbook_BinaryDataObject.json").getFile();
        pb = PlaybookBuilder.getPlaybook(jsonFile);
        SchedulerEngine se = new SchedulerEngine();
        se.execute(pb, pm);
        return atgi;
    }

}
