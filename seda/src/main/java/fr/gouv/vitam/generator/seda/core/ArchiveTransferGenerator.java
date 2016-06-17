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
package fr.gouv.vitam.generator.seda.core;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

import fr.gouv.culture.archivesdefrance.seda.v2.ArchivalAgencyTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.ArchiveUnitType;
import fr.gouv.culture.archivesdefrance.seda.v2.ArchiveUnitTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.CodeListVersionsTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.CodeType;
import fr.gouv.culture.archivesdefrance.seda.v2.DataObjectRefType;
import fr.gouv.culture.archivesdefrance.seda.v2.DescriptiveMetadataContentType;
import fr.gouv.culture.archivesdefrance.seda.v2.IdentifierType;
import fr.gouv.culture.archivesdefrance.seda.v2.LevelType;
import fr.gouv.culture.archivesdefrance.seda.v2.OrganizationWithIdType;
import fr.gouv.culture.archivesdefrance.seda.v2.TextType;
import fr.gouv.culture.archivesdefrance.seda.v2.TransferringAgencyTypeRoot;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.helper.XMLWriterUtils;
import fr.gouv.vitam.generator.seda.helper.ZipFileWriter;

/**
 * This class manages the creation of the SEDA ArchiveTransfer file .It creates the PKZIP file with the manifest.xml SEDA file and the Binary Objects
 */
public class ArchiveTransferGenerator {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ArchiveTransferGenerator.class);
    private static final String SEDA_NAMESPACE = "fr:gouv:culture:archivesdefrance:seda:v2.0";
    private static final String ENCODING = "UTF-8";
    private static final String SEDA_FILENAME = "manifest.xml";
    private ZipFileWriter zipFile;
    private JsonNode globalParameters;
    private DataObjectGroupUsedMap dataObjectGroupUsedMap;
    private Map<String, ArchiveUnitTypeRoot> mapArchiveUnit;
    private XMLStreamWriter writer;


    /**
     * 
     * @param zipFileName : name of the Zip file that will be created
     * @throws VitamSedaException
     */
    public ArchiveTransferGenerator(String zipFileName) throws VitamSedaException {
        ParametersChecker.checkParameter("xmlname cannot be null", zipFileName);
        XMLOutputFactory output = XMLOutputFactory.newInstance();
        dataObjectGroupUsedMap = new DataObjectGroupUsedMap();
        mapArchiveUnit = new LinkedHashMap<>();
        try {
            FileOutputStream fos = new FileOutputStream(SEDA_FILENAME);
            this.writer = output.createXMLStreamWriter(fos, ENCODING);
        } catch (IOException | XMLStreamException e) {
            throw new VitamSedaException("Error on writing to" + SEDA_FILENAME, e);
        }
        try{
            zipFile = new ZipFileWriter(zipFileName);
        }catch (FileNotFoundException e){
            throw new VitamSedaException("Error on writing to" + zipFile, e);
        }
    }

    /**
     * Generate the Head of the xml Seda File : All elements up to DataObjectPackage (excluded)
     * 
     * @param headerfile
     * @throws XMLStreamException
     */

    public void generateHeader(String headerfile) throws XMLStreamException, VitamSedaException {
        ParametersChecker.checkParameter("headerfile cannot be null", headerfile);
        writer.writeStartDocument();
        writer.writeStartElement("ArchiveTransfer");
        writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
        writer.writeNamespace("pr", "info:lc/xmlns/premis-v2");
        writer.writeDefaultNamespace(SEDA_NAMESPACE);
        writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
            SEDA_NAMESPACE + " seda-2.0-main.xsd");
        XMLWriterUtils.setID(writer);
        try {
            globalParameters = JsonHandler.getFromFile(new File(headerfile));
        } catch (InvalidParseOperationException e) {
            throw new VitamSedaException("Error on header file" + headerfile,e);
        }

        getJSONArgument2XML("Comment");
        XMLWriterUtils.writeAttributeValue(writer, "Date", XMLWriterUtils.getDate());
        getJSONArgument2XML("MessageIdentifier");
        getJSONArgument2XML("ArchivalAgreement");
        if (globalParameters.has("CodeListVersions")) {
            CodeListVersionsTypeRoot clvt = getCodeListVersionsType(globalParameters.get("CodeListVersions"));
            writeXMLFragment(clvt);
        }
        startDataObjectPackage();
    }

    /**
     * Beginning of the DataObjectPackage block
     * 
     * @throws XMLStreamException
     */

    public void startDataObjectPackage() throws XMLStreamException {
        writer.writeStartElement("DataObjectPackage");
        XMLWriterUtils.setID(writer);
    }
   
    
    /**
     * Define an archive with 2 elements : title and description
     * 
     * @param title
     * @param description
     * @return the XML:ID of the archive Unit
     */

    public String addArchiveUnit(String title, String description) {
        ParametersChecker.checkParameter("title cannot be null", title);
        ParametersChecker.checkParameter("description cannot be null", description);
        String id = XMLWriterUtils.getNextID();
        ArchiveUnitTypeRoot autr = new ArchiveUnitTypeRoot();
        TextType textTypeTitle = new TextType();
        textTypeTitle.setValue(title);
        TextType textTypeDescription = new TextType();
        textTypeDescription.setValue(description);
        DescriptiveMetadataContentType dmct = new DescriptiveMetadataContentType();
        dmct.getTitle().add(textTypeTitle);
        dmct.getDescription().add(textTypeDescription);
        dmct.setDescriptionLevel(LevelType.RECORD_GRP);
        autr.getContent().add(dmct);
        autr.setId(id);
        mapArchiveUnit.put(id, autr);
        return id;
    }

    /**
     * Set transactedDate to the ArchiveUnit Descriptive Metadata
     * @param id : id of the ArchiveUnit
     * @param date : Date to be set
     */
    public void setTransactedDate(String id,Date date){
        ParametersChecker.checkParameter("id cannot be null", id);
        ParametersChecker.checkParameter("id must be a valid ArchiveUnit ID", mapArchiveUnit.get(id));
        ArchiveUnitTypeRoot autr = mapArchiveUnit.get(id);
        for (DescriptiveMetadataContentType dmct: autr.getContent()){
            autr.getContent().remove(dmct);
            dmct.setTransactedDate(XMLWriterUtils.getDate(date));
            autr.getContent().add(dmct);
        }
    }
    
    /**
     * Remove an ArchiveUnit from the collection . 
     * It doesn't destroy the relation with other ArchiveUnits
     * @param id
     */
    public void removeArchiveUnit(String id) {
        ParametersChecker.checkParameter("id cannot be null", id);
        mapArchiveUnit.remove(id);
    }

    /**
     * Add a relation between 2 archive Units
     * 
     * @param archiveUnitFatherID
     * @param archiveUnitSonID
     */
    public void addArchiveUnit2ArchiveUnitReference(String archiveUnitFatherID, String archiveUnitSonID) {
        ParametersChecker.checkParameter("archiveUnitFatherID cannot be null", archiveUnitFatherID);
        ParametersChecker.checkParameter("archiveUnitSonID cannot be null", archiveUnitSonID);
        ArchiveUnitTypeRoot autrFather = mapArchiveUnit.get(archiveUnitFatherID);
        ArchiveUnitType autSon = new ArchiveUnitType();
        autSon.setId(XMLWriterUtils.getNextID());
        autSon.setArchiveUnitRefId(archiveUnitSonID);
        autrFather.getArchiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference().add(autSon);
    }

    /**
     * Add a relation between an archive unit and a DataObjectGroup
     * 
     * @param archiveUnitFatherID
     * @param dataobjectGroupSonID
     */
    public void addArchiveUnit2DataObjectGroupReference(String archiveUnitFatherID, String dataobjectGroupSonID) {
        ParametersChecker.checkParameter("archiveUnitFatherID cannot be null", archiveUnitFatherID);
        ParametersChecker.checkParameter("dataobjectGroupSonID cannot be null", dataobjectGroupSonID);
        ArchiveUnitTypeRoot autrFather = mapArchiveUnit.get(archiveUnitFatherID);
        DataObjectRefType dort = new DataObjectRefType();
        dort.setId(XMLWriterUtils.getNextID());
        dort.setDataObjectGroupReferenceId(dataobjectGroupSonID);
        autrFather.getArchiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference().add(dort);
        // When an ArchiveUnit has a DataObjectGroup, it ia at the Level FILE
        for (DescriptiveMetadataContentType dmct: autrFather.getContent()){
            autrFather.getContent().remove(dmct);
            dmct.setDescriptionLevel(LevelType.FILE);
            autrFather.getContent().add(dmct);
        }
    }



    /**
     * Write the Description MetaData section . It must be done when all the Archive unit have been added but before the
     * Management Metadata
     * 
     * @throws XMLStreamException
     */
    public void writeDescriptiveMetadata() throws VitamSedaException {
        try{
            writer.writeStartElement("DescriptiveMetadata");
        }catch(XMLStreamException e){
            throw new VitamSedaException("Can't write Descriptive Medatadata", e); 
        }
        for (ArchiveUnitTypeRoot autr : mapArchiveUnit.values()) {
            writeXMLFragment(autr);
        }
        try{
            writer.writeEndElement();
        }catch(XMLStreamException e){
            throw new VitamSedaException("Can't write Descriptive Medatadata", e); 
        }
    }

    /**
     * Write the Management Metadata section
     * 
     * @throws XMLStreamException
     */

    public void writeManagementMetadata() throws XMLStreamException {
        writer.writeEmptyElement("ManagementMetadata");
    }

    /**
     * Write the end of the document (close DataObjectPackage, write ArchivalAgency and Transferring Agency)
     * 
     * @throws XMLStreamException
     */

    public void closeDocument() throws XMLStreamException,VitamSedaException {
        // DataObjectPackage
        writer.writeEndElement();
        getJSONArgument2XML("ArchivalAgency", new ArchivalAgencyTypeRoot());
        getJSONArgument2XML("TransferringAgency", new TransferringAgencyTypeRoot());
        writer.writeEndDocument();
        writer.close();
        try{
            zipFile.addFile(SEDA_FILENAME);
            zipFile.closeZipFile();
        }catch(IOException e){
            throw new VitamSedaException("Error to write to zipFile",e);
        }
        
    }

    
    /**
     * Serialize a Jaxb POJO object in the current XML stream
     * @param jaxbPOJO
     * @throws VitamSedaException
     */
    public void writeXMLFragment(Object jaxbPOJO) throws VitamSedaException{
        try {
            MarshallerObjectCache.getMarshaller(jaxbPOJO.getClass()).marshal(jaxbPOJO, writer);
        } catch (JAXBException e) {
            throw new VitamSedaException("Error on writing " + jaxbPOJO + "object", e);
        }

    }


    
    /**
     * Parse the node CodeListVersion of the json header file and do the binding with the pojo
     * 
     * @param jsonNode
     * @return
     */
    private CodeListVersionsTypeRoot getCodeListVersionsType(JsonNode jsonNode) throws VitamSedaException {
        CodeListVersionsTypeRoot clvt = new CodeListVersionsTypeRoot();
        ObjectMapper mapper = new ObjectMapper();
        Map<String, Object> result = mapper.convertValue(jsonNode, Map.class);
        for (String key : result.keySet()) {
            CodeType ct = new CodeType();
            Object value = result.get(key);
            if (value instanceof String) {
                ct.setValue((String) result.get(key));
                try {
                    //
                    clvt.getClass().getMethod("set" + key, CodeType.class).invoke(clvt, ct);
                } catch (NoSuchMethodException e) { //NOSONAR : it is just a warning based on a bad named argument given in the Json File
                    LOGGER.warn("Argument" + key + "is not a CodeListVersion argument");
                } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
                    throw new VitamSedaException("Error on assigning " + key + "argument", e);
                }
            }
        }
        return clvt;
    }

    private void getJSONArgument2XML(String key) throws XMLStreamException {
        if (globalParameters.has(key) && globalParameters.get(key).getNodeType().equals(JsonNodeType.STRING)) {
            XMLWriterUtils.writeAttributeValue(writer, key, globalParameters.get(key).textValue());
        }
    }

    private void getJSONArgument2XML(String key, OrganizationWithIdType owit) throws VitamSedaException{
        if (globalParameters.has(key)) {
            JsonNode jsonSonNode = globalParameters.get(key);
            String identifierKey = "Identifier";
            if (jsonSonNode.has(identifierKey) &&
                jsonSonNode.get(identifierKey).getNodeType().equals(JsonNodeType.STRING)) {
                IdentifierType it = new IdentifierType();
                it.setValue(jsonSonNode.get(identifierKey).asText());
                owit.setIdentifier(it);
                try{
                    writeXMLFragment(owit);
                }catch (VitamSedaException e){
                    throw new VitamSedaException("Can't write OrganizationWithIdType", e); 
                }
            }
        }
    }

    /**
     * @return the dataObjectGroupUsedMap
     */
    public DataObjectGroupUsedMap getDataObjectGroupUsedMap() {
        return dataObjectGroupUsedMap;
    }

    /**
     * @return the zipFile
     */
    public ZipFileWriter getZipFile() {
        return zipFile;
    }
}
