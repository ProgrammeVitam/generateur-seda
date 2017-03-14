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
import java.nio.file.FileSystems;
import java.util.Collection;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import javax.xml.bind.JAXBException;
import javax.xml.stream.XMLStreamException;

import org.codehaus.stax2.XMLOutputFactory2;
import org.codehaus.stax2.XMLStreamWriter2;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;

import fr.gouv.culture.archivesdefrance.seda.v2.ArchivalAgencyTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.ArchiveUnitType;
import fr.gouv.culture.archivesdefrance.seda.v2.ArchiveUnitTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.CodeListVersionsTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.CodeType;
import fr.gouv.culture.archivesdefrance.seda.v2.DataObjectRefTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.DescriptiveMetadataContentType;
import fr.gouv.culture.archivesdefrance.seda.v2.DescriptiveMetadataContentTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.IdentifierType;
import fr.gouv.culture.archivesdefrance.seda.v2.LevelType;
import fr.gouv.culture.archivesdefrance.seda.v2.ManagementMetadataTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.ManagementRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.OrganizationWithIdType;
import fr.gouv.culture.archivesdefrance.seda.v2.TextType;
import fr.gouv.culture.archivesdefrance.seda.v2.TransferringAgencyTypeRoot;
import fr.gouv.vitam.common.CharsetUtils;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaMissingFieldException;
import fr.gouv.vitam.generator.seda.helper.XMLWriterUtils;
import fr.gouv.vitam.generator.seda.helper.ZipFileWriter;

//import fr.gouv.culture.archivesdefrance.seda.v2.ArchiveUnitType;


/**
 * This class manages the creation of the SEDA ArchiveTransfer file .It creates the PKZIP file with the manifest.xml SEDA file and the Binary Objects
 */
public class ArchiveTransferGenerator {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ArchiveTransferGenerator.class);
    private static final String SEDA_NAMESPACE = "fr:gouv:culture:archivesdefrance:seda:v2.0";
    private static final String ENCODING = CharsetUtils.UTF_8;
    private static final String SEDA_FILENAME = "manifest.xml";
    private final String temporarySedaFilePath;
    private final ZipFileWriter zipFile;
    private final DataObjectGroupUsedMap dataObjectGroupUsedMap;
    private final Map<String, ArchiveUnitTypeRoot> mapArchiveUnit;
    private final FileOutputStream writerFOS;
    private final XMLStreamWriter2 writer;
    private final ArchiveTransferConfig archiveTransferConfig;

    private Multimap<String, String> edges = ArrayListMultimap.create();

    /**
     * @param archiveTransferConfig : contains the global configuration of the ArchiveTransfer
     * @param zipFileName : name of the Zip file that will be created
     * @throws VitamSedaException
     * @throws IllegalArgumentException if zipFileName is null
     * TODO pour chaque méthode utilisant checkParameter: ajouter dans la JavaDoc @throws IllegalArgumentException et les noms des arguments vérifiés
     */
    public ArchiveTransferGenerator(ArchiveTransferConfig archiveTransferConfig, String zipFileName)
        throws VitamSedaException {
        ParametersChecker.checkParameter("archiveTransferConfig cannot be null", archiveTransferConfig);
        ParametersChecker.checkParameter("xmlname cannot be null", zipFileName);
        XMLOutputFactory2 output = (XMLOutputFactory2) XMLOutputFactory2.newInstance();
        dataObjectGroupUsedMap = new DataObjectGroupUsedMap();
        mapArchiveUnit = new LinkedHashMap<>();
        this.archiveTransferConfig = archiveTransferConfig;
        try {
            temporarySedaFilePath =
                System.getProperty("java.io.tmpdir") + FileSystems.getDefault().getSeparator() + SEDA_FILENAME;
            writerFOS = new FileOutputStream(temporarySedaFilePath);
            this.writer = (XMLStreamWriter2) output.createXMLStreamWriter(writerFOS, ENCODING);
        } catch (IOException | XMLStreamException e) {
            throw new VitamSedaException("Error on writing to" + SEDA_FILENAME, e);
        }
        try {
            zipFile = new ZipFileWriter(zipFileName);
            zipFile.addDirectory("Content/");
        } catch (FileNotFoundException e) {

            throw new VitamSedaException("Error on writing to" + zipFileName, e);
        } catch (IOException e) {
            throw new VitamSedaException("Can't create /Content directory in the zipFile", e);
        }
    }


    /**
     * Generate the Head of the xml Seda File : All elements up to DataObjectPackage (excluded)
     *
     * @throws XMLStreamException
     * @throws VitamSedaException
     * @throws IllegalArgumentException if headerfile is null
     */

    public void generateHeader() throws XMLStreamException, VitamSedaException {
        writer.writeStartDocument();
        writer.writeStartElement("ArchiveTransfer");
        writer.writeNamespace("xlink", "http://www.w3.org/1999/xlink");
        writer.writeNamespace("pr", "info:lc/xmlns/premis-v2");
        writer.writeDefaultNamespace(SEDA_NAMESPACE);
        writer.writeNamespace("xsi", "http://www.w3.org/2001/XMLSchema-instance");
        writer.writeAttribute("xsi", "http://www.w3.org/2001/XMLSchema-instance", "schemaLocation",
            SEDA_NAMESPACE + " seda-2.0-main.xsd");
        XMLWriterUtils.setID(writer);
        getJSONArgument2XML("Comment", false);
        XMLWriterUtils.writeAttributeValue(writer, "Date", XMLWriterUtils.getDate());
        getJSONArgument2XML("MessageIdentifier", true);
        getJSONArgument2XML("ArchivalAgreement", false);
        if (archiveTransferConfig.has("CodeListVersions")) {
            CodeListVersionsTypeRoot clvt = getCodeListVersionsType(archiveTransferConfig.get("CodeListVersions"));
            writeXMLFragment(clvt);
        } else {
            throw new VitamSedaMissingFieldException("Missing CodeListVersions in the configuration File");
        }
        writer.writeStartElement("DataObjectPackage");
        XMLWriterUtils.setID(writer);
    }

    /**
     * Define an archive with 2 elements : title and description
     *
     * @param title
     * @param description
     * @return the XML:ID of the archive Unit
     * @throws IllegalArgumentException if title or description is null
     */

    public String addArchiveUnit(String title, String description) {
        ParametersChecker.checkParameter("title cannot be null", title);
        ParametersChecker.checkParameter("description cannot be null", description);
        return addArchiveUnit(title, description, null);
    }

    /**
     * efine an archive with 3 elements : title and description and a json metadata File
     *
     * @param title
     * @param description
     * @param metadataFile
     * @return the XML:ID of the archive Unit
     */
    public String addArchiveUnit(String title, String description, File metadataFile) {
        ParametersChecker.checkParameter("title cannot be null", title);
        ParametersChecker.checkParameter("description cannot be null", description);
        String id = XMLWriterUtils.getNextID();
        ArchiveUnitTypeRoot autr = new ArchiveUnitTypeRoot();
        TextType textTypeTitle = new TextType();
        textTypeTitle.setValue(title);
        TextType textTypeDescription = new TextType();
        textTypeDescription.setValue(description);
        DescriptiveMetadataContentTypeRoot dmct = new DescriptiveMetadataContentTypeRoot();
        ManagementRoot management = new ManagementRoot();
        try {
            if (metadataFile != null) {
                JsonNode jn = JsonHandler.getFromFile(metadataFile);
                if (jn.has("Content")) {
                    dmct = JsonHandler.getFromJsonNode(jn.get("Content"), DescriptiveMetadataContentTypeRoot.class);
                }
                if (jn.has("Management")) {
                    management = JsonHandler.getFromJsonNode(jn.get("Management"), ManagementRoot.class);
                    management.unmarshallFromJson();
                    autr.setManagement(management);
                }
            }
        } catch (InvalidParseOperationException e) {
            LOGGER.warn("File " + metadataFile + " is not a valid json File for Content or Management Metadata", e);
        }
        if (dmct.getTitle().isEmpty()) {
            dmct.getTitle().add(textTypeTitle);
        }
        if (dmct.getDescription().isEmpty()) {
            dmct.getDescription().add(textTypeDescription);
        }
        if (dmct.getDescriptionLevel() == null) {
            dmct.setDescriptionLevel(LevelType.RECORD_GRP);
        }
        autr.getContent().add(dmct);
        autr.setId(id);
        mapArchiveUnit.put(id, autr);
        return id;
    }

    /**
     * Set the rawManagementFile
     *
     * @param archiveUnitID
     * @param rawmetadataFile
     */
    public void addRawManagementFile(String archiveUnitID, File rawmetadataFile) {
        ArchiveUnitTypeRoot au = mapArchiveUnit.get(archiveUnitID);
        if (au != null) {
            au.setRawManagementFile(rawmetadataFile);
        }
    }

    /**
     * Set the rawContentFile
     *
     * @param archiveUnitID
     * @param rawContentFile
     */
    public void addRawContentFile(String archiveUnitID, File rawContentFile) {
        ArchiveUnitTypeRoot au = mapArchiveUnit.get(archiveUnitID);
        if (au != null) {
            au.setRawContentFile(rawContentFile);
        }
    }

    /**
     * Set transactedDate to the ArchiveUnit Descriptive Metadata
     *
     * @param id : id of the ArchiveUnit
     * @param date : Date to be set
     * @throws IllegalArgumentException if id is null or isn't a valid ArchiveUnit Id
     */
    public void setTransactedDate(String id, Date date) {
        ParametersChecker.checkParameter("id cannot be null", id);
        ParametersChecker.checkParameter("id must be a valid ArchiveUnit ID", mapArchiveUnit.get(id));
        ArchiveUnitTypeRoot autr = mapArchiveUnit.get(id);
        for (DescriptiveMetadataContentType dmct : autr.getContent()) {
            autr.getContent().remove(dmct);
            dmct.setTransactedDate(XMLWriterUtils.getDate(date));
            autr.getContent().add(dmct);
        }
        autr.setStartDate(date);
        autr.setEndDate(date);
    }

    /**
     * Remove an ArchiveUnit from the collection .
     * It doesn't destroy the relation with other ArchiveUnits
     *
     * @param id
     * @throws IllegalArgumentException if id is null
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
     * @throws IllegalArgumentException if archiveUnitFatherID or archiveUnitSonID are null
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
     * @throws IllegalArgumentException if archiveUnitFatherID or dataobjectGroupSonID are null
     */
    public void addArchiveUnit2DataObjectGroupReference(String archiveUnitFatherID, String dataobjectGroupSonID) {
        ParametersChecker.checkParameter("archiveUnitFatherID cannot be null", archiveUnitFatherID);
        ParametersChecker.checkParameter("dataobjectGroupSonID cannot be null", dataobjectGroupSonID);
        ArchiveUnitTypeRoot autrFather = mapArchiveUnit.get(archiveUnitFatherID);
        DataObjectRefTypeRoot dort = new DataObjectRefTypeRoot();
        //        dort.setId(XMLWriterUtils.getNextID());
        dort.setDataObjectGroupReferenceId(dataobjectGroupSonID);
        autrFather.getArchiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference().add(dort);
        // When an ArchiveUnit has a DataObjectGroup, it is at the Level FILE
        for (DescriptiveMetadataContentType dmct : autrFather.getContent()) {
            autrFather.getContent().remove(dmct);
            dmct.setDescriptionLevel(LevelType.ITEM);
            autrFather.getContent().add(dmct);
        }
    }

    /**
     * Write the Description MetaData section . It must be done when all the Archive unit have been added but before the
     * Management Metadata
     *
     * @return number of written archive units
     * @throws VitamSedaException
     */
    public int writeDescriptiveMetadata() throws VitamSedaException {
        int nbArchiveUnits = 0;
        try {
            writer.writeStartElement("DescriptiveMetadata");
        } catch (XMLStreamException e) {
            throw new VitamSedaException("Can't write Descriptive Medatadata", e);
        }
        try {
            for (ArchiveUnitTypeRoot autr : mapArchiveUnit.values()) {
                //writeXMLFragment(autr);
                if (edges.containsKey(autr.getId())) {
                    Collection<String> references = edges.get(autr.getId());

                    for (String reference : references) {
                        ArchiveUnitType archiveUnitArc = new ArchiveUnitType();
                        String id = XMLWriterUtils.getNextID();
                        archiveUnitArc.setId(id);
                        archiveUnitArc.setArchiveUnitRefId(reference);

                        autr.getArchiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference()
                            .add(archiveUnitArc);
                    }
                }
                autr.marshall(writer);
                nbArchiveUnits++;
            }
            writer.writeEndElement();
            return nbArchiveUnits;
        } catch (XMLStreamException e) {
            throw new VitamSedaException("Can't write Descriptive Medatadata", e);
        }
    }

    /**
     * Write the Management Metadata section
     *
     * @throws XMLStreamException
     */

    public void writeManagementMetadata() throws XMLStreamException {
        ManagementMetadataTypeRoot mmtr = new ManagementMetadataTypeRoot();
        JsonNode mmjn = archiveTransferConfig.get("ManagementMetadata");
        if (mmjn != null) {
            try {
                mmtr = JsonHandler.getFromJsonNode(mmjn, ManagementMetadataTypeRoot.class);
                mmtr.unmarshallFromJson();
            } catch (InvalidParseOperationException e) {
                LOGGER.warn(
                    "ManagementMetadataKey in the config file is not a valid json File for ManagementMetadata Section",
                    e);
            }
        }
        mmtr.setOriginatingAgencyIdentifier(
            archiveTransferConfig.getString("ManagementMetadata.OriginatingAgencyIdentifier"));
        mmtr.setSubmissionAgencyIdentifier(
            archiveTransferConfig.getString("ManagementMetadata.SubmissionAgencyIdentifier"));
        try {
            writeXMLFragment(mmtr);
        } catch (VitamSedaException e) {
            LOGGER.error(e);
        }
    }

    /**
     * Write the end of the document (close DataObjectPackage, write ArchivalAgency and Transferring Agency)
     *
     * @throws XMLStreamException
     * @throws VitamSedaException
     */

    public void closeDocument() throws XMLStreamException, VitamSedaException {
        // DataObjectPackage closing
        writer.writeEndElement();
        getJSONArgument2XML("ArchivalAgency", new ArchivalAgencyTypeRoot(), true);
        getJSONArgument2XML("TransferringAgency", new TransferringAgencyTypeRoot(), true);
        writer.writeEndDocument();
        writer.close();
        try {
            writerFOS.close();
            zipFile.addFile(SEDA_FILENAME, temporarySedaFilePath);
            zipFile.closeZipFile();

        } catch (IOException e) {
            throw new VitamSedaException("Error to write to zipFile", e);
        }
        if (!new File(temporarySedaFilePath).delete()) {
            LOGGER.warn("Error to remove temporary manifest file " + temporarySedaFilePath);
        }

    }


    /**
     * Serialize a Jaxb POJO object in the current XML stream
     *
     * @param jaxbPOJO
     * @throws VitamSedaException
     */
    public void writeXMLFragment(Object jaxbPOJO) throws VitamSedaException {
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
        for (Entry<String, Object> entry : result.entrySet()) {
            CodeType ct = new CodeType();
            Object value = entry.getValue();
            if (value instanceof String) {
                ct.setValue((String) value);
                try {
                    // Introspection . Must be changed by an Object Mapper
                    clvt.getClass().getMethod("set" + entry.getKey(), CodeType.class).invoke(clvt, ct);
                } catch (NoSuchMethodException e) { //NOSONAR : it is just a warning based on a bad named argument given in the Json File
                    LOGGER.warn("Argument" + entry.getKey() + "is not a CodeListVersion argument");
                } catch (InvocationTargetException | IllegalArgumentException | IllegalAccessException e) {
                    throw new VitamSedaException("Error on assigning " + entry.getKey() + "argument", e);
                }
            }
        }
        if ((clvt.getReplyCodeListVersion() == null) ||
            (clvt.getMessageDigestAlgorithmCodeListVersion() == null) ||
            (clvt.getFileFormatCodeListVersion() == null)) {
            throw new VitamSedaMissingFieldException(
                "In the CodeListVersion, one of the 3 mandatory element (ReplyCodeListVersion, MessageDigestAlgorithmCodeListVersion, FileFormatCodeListVersion)  is missing");
        }
        return clvt;
    }

    private void getJSONArgument2XML(String key, boolean required) throws XMLStreamException {
        if (archiveTransferConfig.has(key) &&
            archiveTransferConfig.get(key).getNodeType().equals(JsonNodeType.STRING)) {
            XMLWriterUtils.writeAttributeValue(writer, key, archiveTransferConfig.get(key).textValue());
        } else if (required) {
            throw new VitamSedaMissingFieldException(
                key + " is mandatory in the SEDA. Add this parameter to configuration file ");
        }
    }

    private void getJSONArgument2XML(String key, OrganizationWithIdType owit, boolean required)
        throws VitamSedaException {
        if (archiveTransferConfig.has(key)) {
            JsonNode jsonSonNode = archiveTransferConfig.get(key);
            String identifierKey = "Identifier";
            if (jsonSonNode.has(identifierKey) &&
                jsonSonNode.get(identifierKey).getNodeType().equals(JsonNodeType.STRING)) {
                IdentifierType it = new IdentifierType();
                it.setValue(jsonSonNode.get(identifierKey).asText());
                owit.setIdentifier(it);
                try {
                    writeXMLFragment(owit);
                } catch (VitamSedaException e) {
                    throw new VitamSedaException("Can't write OrganizationWithIdType", e);
                }
            }
        } else if (required) {
            throw new VitamSedaMissingFieldException(
                key + "is mandatory in the SEDA. Add this parameter to configuration file ");
        }
    }

    /**
     * Calculate the Start and End Date of an ArchiveUnit
     *
     * @param archiveUnitID
     */
    public void addStartAndEndDate2ArchiveUnit(String archiveUnitID) {
        ParametersChecker.checkParameter("Archive Unit ID cannot be null", archiveUnitID);
        ArchiveUnitTypeRoot autr = mapArchiveUnit.get(archiveUnitID);
        Date startDate = null;
        Date endDate = null;
        for (Object aut : autr.getArchiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference()) {
            if (aut instanceof DataObjectRefTypeRoot) {
                // Do Nothing: No dates in DOG
                continue;
            }

            ArchiveUnitTypeRoot sonautr = mapArchiveUnit.get(((ArchiveUnitType) aut).getArchiveUnitRefId());
            if (sonautr != null) {

                // Initialize the start and endDate
                // Take the minimum date of the sons
                if (sonautr.getStartDate() != null &&
                    (startDate == null || sonautr.getStartDate().compareTo(startDate) < 0)) {
                    startDate = sonautr.getStartDate();
                }
                // Take the maximum date of the sons
                if (sonautr.getEndDate() != null && (endDate == null || sonautr.getEndDate().compareTo(endDate) > 0)) {
                    endDate = sonautr.getEndDate();
                }

            } else {
                LOGGER.info(archiveUnitID + " is an unknown Archive Unit ID");
            }
        }
        for (DescriptiveMetadataContentType dmct : autr.getContent()) {
            autr.getContent().remove(dmct);
            dmct.setStartDate(XMLWriterUtils.getDate(startDate));
            dmct.setEndDate(XMLWriterUtils.getDate(endDate));
            autr.getContent().add(dmct);
        }
        autr.setStartDate(startDate);
        autr.setEndDate(endDate);
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

    @VisibleForTesting
    Map<String, ArchiveUnitTypeRoot> getMapArchiveUnit() {
        return mapArchiveUnit;
    }

    public void addEdge(String fatherId, String archiveUnitRefId) {
        ParametersChecker.checkParameter("archiveUnitFatherID cannot be null", fatherId);
        ParametersChecker.checkParameter("archiveUnitRefId cannot be null", archiveUnitRefId);

        edges.put(fatherId, archiveUnitRefId);
    }

}
