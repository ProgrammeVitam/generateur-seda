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

import java.io.FileWriter;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fr.gouv.culture.archivesdefrance.seda.v2.ArchiveUnitType;
import fr.gouv.culture.archivesdefrance.seda.v2.ArchiveUnitTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.DataObjectRefType;
import fr.gouv.culture.archivesdefrance.seda.v2.DescriptiveMetadataContentType;
import fr.gouv.culture.archivesdefrance.seda.v2.FormatIdentificationType;
import fr.gouv.culture.archivesdefrance.seda.v2.LevelType;
import fr.gouv.culture.archivesdefrance.seda.v2.MessageDigestBinaryObjectType;
import fr.gouv.culture.archivesdefrance.seda.v2.TextType;
import fr.gouv.vitam.generator.seda.helper.FileUtils;
import fr.gouv.vitam.generator.seda.helper.JsonTransformUtils;
import fr.gouv.vitam.generator.seda.helper.XMLWriterUtils;

/**
 * 
 */
public class ArchiveTransferGeneratorImpl {

    private XMLStreamWriter writer;
    private static final String SEDA_NAMESPACE = "fr:gouv:culture:archivesdefrance:seda:v2.0";
    private static final String DIGEST_ALGORITHM = "SHA-512";
    private String globalParameters;
    private Map<String,Boolean> mapDataObjectGroup;
    private Map<String,ArchiveUnitTypeRoot> mapArchiveUnit;
    
    public ArchiveTransferGeneratorImpl(String xmlName){
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        mapDataObjectGroup = new HashMap<>();
        mapArchiveUnit = new HashMap<>();
        try{
        this.writer = output.createXMLStreamWriter(new FileWriter(xmlName));
        }catch(IOException e){
            e.printStackTrace();
        }catch(XMLStreamException e){
            e.printStackTrace();
        }
    }
     
    /**
     * Generate the Head of the xml Seda File : All elements up to DataObjectPackage (excluded)
     * @param headerfile
     * @throws XMLStreamException
     */
    
    public void generateHeader(String headerfile) throws XMLStreamException{
        globalParameters=headerfile;
        writer.writeStartDocument();
        writer.writeStartElement("ArchiveTransfer");
        writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
        writer.writeNamespace("pr", "info:lc/xmlns/premis-v2");
        writer.writeDefaultNamespace(SEDA_NAMESPACE);
        writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("xsi","http://www.w3.org/2001/XMLSchema-instance", "schemaLocation", SEDA_NAMESPACE+" seda-2.0-main.xsd");
        XMLWriterUtils.setID(writer);
        JsonTransformUtils.Json2XML(writer, headerfile,"Comment");
        XMLWriterUtils.writeAttributeValue(writer, "Date", XMLWriterUtils.getDate());
        JsonTransformUtils.Json2XML(writer, headerfile,"MessageIdentifier");
        JsonTransformUtils.Json2XML(writer, headerfile,"ArchivalAgreement");
        JsonTransformUtils.Json2XML(writer, headerfile,"CodeListVersions");
        startDataObjectPackage();
    }
    
    /**
     * Beginning of the DataObjectPackage block
     * @throws XMLStreamException
     */
    
    public void startDataObjectPackage() throws XMLStreamException{
        writer.writeStartElement("DataObjectPackage");
        XMLWriterUtils.setID(writer);
    }
   
    
    /**
     * Add a binary data objcet to a specific dataObjectGroup
     * @param uri
     * @param filename
     * @param dataObjectGroupID
     * @throws XMLStreamException
     */
    
    public void addBinaryDataObject(String uri,String filename,String dataObjectGroupID) throws XMLStreamException{
        BinaryDataObjectTypeRoot bdotr = new  BinaryDataObjectTypeRoot();
        // The given dogID is incorrect . we create a new ID
        if (dataObjectGroupID == null || (! existsDataObjectGroup(dataObjectGroupID))){
            String newID = registerDataObjectGroup();
            bdotr.setDataObjectGroupId(newID);
            mapDataObjectGroup.put(newID, new Boolean(true));
        // The given dogID has already be used 
        }else if (mapDataObjectGroup.get(dataObjectGroupID).booleanValue()){
            bdotr.setDataObjectGroupReferenceId(dataObjectGroupID);
        }else{
            bdotr.setDataObjectGroupId(dataObjectGroupID);
            mapDataObjectGroup.put(dataObjectGroupID,new Boolean(true));
        }
        
        bdotr.setDataObjectVersion("DataObjectVersion0");
        FileUtils f =  new FileUtils(filename, DIGEST_ALGORITHM);
        bdotr.setId(XMLWriterUtils.getNextID());
        bdotr.setUri(uri);
        MessageDigestBinaryObjectType mdbot = new MessageDigestBinaryObjectType();
        mdbot.setAlgorithm(DIGEST_ALGORITHM);
        mdbot.setValue(Base64.getEncoder().encodeToString(f.getMessageDigest().digest()));
        bdotr.setMessageDigest(mdbot);
        bdotr.setSize(new BigInteger(String.valueOf(f.getFilesize())));
        bdotr.setFormatIdentification(new FormatIdentificationType());
        writeXMLFragment(bdotr);
    }
    
    /**
     * Define an archive with 2 elements : title and description
     * @param title
     * @param description
     * @return the XML:ID of the archive Unit
     */
    
    public String addArchiveUnit(String title, String description){
        String id= XMLWriterUtils.getNextID();
        ArchiveUnitTypeRoot autr = new ArchiveUnitTypeRoot();
        autr.setId(id);
        TextType textTypeTitle = new TextType();
        textTypeTitle.setValue(title);
        TextType textTypeDescription = new TextType();
        textTypeDescription.setValue(description);
        DescriptiveMetadataContentType dmct = new DescriptiveMetadataContentType();
        dmct.getTitle().add(textTypeTitle);
        dmct.getDescription().add(textTypeDescription);
        dmct.setDescriptionLevel(LevelType.RECORD_GRP);
        autr.getContent().add(dmct);
        mapArchiveUnit.put(id, autr);
        return id;
    }
    
    /**
     * Add a relation between 2 archive Units
     * @param archiveUnitFatherID
     * @param archiveUnitSonID
     */
    public void addArchiveUnit2ArchiveUnitReference(String archiveUnitFatherID, String archiveUnitSonID){
        if (archiveUnitFatherID == null || archiveUnitSonID == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        ArchiveUnitTypeRoot autrFather = mapArchiveUnit.get(archiveUnitFatherID);
        ArchiveUnitType autSon = new ArchiveUnitType();
        autSon.setId(XMLWriterUtils.getNextID());
        autSon.setArchiveUnitRefId(archiveUnitSonID);
        autrFather.getArchiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference().add(autSon);
    }

    /**
     * Add a relation between an archive unit and a DataObjectGroup
     * @param archiveUnitFatherID
     * @param dataobjectGroupSonID
     */
    public void addArchiveUnit2DataObjectGroupReference(String archiveUnitFatherID, String dataobjectGroupSonID){
        if (archiveUnitFatherID == null ||dataobjectGroupSonID == null){
            throw new IllegalArgumentException("Arguments cannot be null");
        }
        ArchiveUnitTypeRoot autrFather = mapArchiveUnit.get(archiveUnitFatherID);
        DataObjectRefType dort = new DataObjectRefType();
        dort.setId(XMLWriterUtils.getNextID());
        dort.setDataObjectGroupReferenceId(dataobjectGroupSonID);
        autrFather.getArchiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference().add(dort);
    }

    
    /**
     * Test if the given id is an existing DataObjectGroup
     * @param id
     * @return
     */
    public boolean existsDataObjectGroup(String id){
        return mapDataObjectGroup.containsKey(id);
    }
    
    /**
     * Register a new DataObjectGroup
     * @return ID of the DataObjectGroup
     */
    public String registerDataObjectGroup(){
        String id = XMLWriterUtils.getNextID();
        mapDataObjectGroup.put(id,new Boolean(false));
        return id;
    }
    
    public void closeDocument() throws XMLStreamException{
        writer.writeStartElement("DescriptiveMetadata");
        for (ArchiveUnitTypeRoot autr: mapArchiveUnit.values()){
            writeXMLFragment(autr);
        }
        writer.writeEndElement();
        writer.writeEmptyElement("ManagementMetadata");
        // DataObjectPackage
        writer.writeEndElement();
        JsonTransformUtils.Json2XML(writer, globalParameters,"ArchivalAgency");
        JsonTransformUtils.Json2XML(writer, globalParameters,"TransferringAgency");
        writer.writeEndDocument();
        writer.close();
    }
    
    private void writeXMLFragment(Object jaxbPOJO){
        try{
            JAXBContext jc = JAXBContext.newInstance(jaxbPOJO.getClass());
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshaller.marshal(jaxbPOJO, writer);
        }catch (Exception e ){
            e.printStackTrace();
        }
    }
    
}