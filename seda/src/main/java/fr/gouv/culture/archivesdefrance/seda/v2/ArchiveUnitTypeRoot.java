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
package fr.gouv.culture.archivesdefrance.seda.v2;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.util.Date;

import javax.xml.bind.JAXBException;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import org.codehaus.stax2.XMLStreamWriter2;

import fr.gouv.vitam.generator.seda.core.MarshallerObjectCache;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.helper.XMLWriterUtils;

/**
 * The override of the generated pojo is needed to describe it as a root element to generate the XML Stream
 */
@XmlRootElement(name = "ArchiveUnit")
public class ArchiveUnitTypeRoot extends ArchiveUnitType {
    @XmlTransient
    private Date startDate;
    @XmlTransient
    private Date endDate;
    @XmlTransient
    private File rawContentFile;
    @XmlTransient
    private File rawManagementFile;
    @XmlTransient
    private static final int BUFFER_SIZE = 64 * 1024;

    /**
     * Default constructor : do nothing (Pojo)
     */
    public ArchiveUnitTypeRoot() {

    }

    protected ArchiveUnitTypeRoot(ArchiveUnitType au) {
        this.id = au.getId();
        this.archiveUnitRefId = au.archiveUnitRefId;
    }



    /**
     * @return the rawContentFile
     */
    public File getRawContentFile() {
        return rawContentFile;
    }

    /**
     * @param rawContentFile the rawContentFile to set
     */
    public void setRawContentFile(File rawContentFile) {
        this.rawContentFile = rawContentFile;
    }

    /**
     * @return the rawManagementFile
     */
    public File getRawManagementFile() {
        return rawManagementFile;
    }

    /**
     * @param rawManagementFile the rawMetadataFile to set
     */
    public void setRawManagementFile(File rawManagementFile) {
        this.rawManagementFile = rawManagementFile;
    }

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     *
     * @return this
     */
    public ArchiveUnitTypeRoot setStartDate(Date startDate) {
        this.startDate = startDate;
        return this;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     *
     * @return this
     */
    public ArchiveUnitTypeRoot setEndDate(Date endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Custom marshalling to xml of the ArchiveUnit
     * @param writer XMLStreamWrite on which we have to write
     * @throws VitamSedaException
     * @throws XMLStreamException
     */
    public void marshall(XMLStreamWriter2 writer) throws VitamSedaException, XMLStreamException {
        if (rawContentFile != null || rawManagementFile != null) {
            manualMarshall(writer);
        } else {
            writeXMLFragment(this, writer);
        }
    }

    private void writeFile(File file, XMLStreamWriter2 writer)
        throws VitamSedaException, XMLStreamException {
        char[] buffer = new char[BUFFER_SIZE];
        int nbRead;
        try (FileInputStream fis = new FileInputStream(file)) {
            try (InputStreamReader isr = new InputStreamReader(fis, Charset.forName("UTF-8"))) {
                while ((nbRead = isr.read(buffer)) != -1) {
                    writer.writeRaw(buffer, 0, nbRead);
                }
            }
        } catch (IOException e) {
            throw new VitamSedaException("Issue to write file" + file + "to XML stream", e);
        }

    }

    private void manualMarshall(XMLStreamWriter2 writer) throws VitamSedaException, XMLStreamException {
        writer.writeStartElement("ArchiveUnit");
        if (id != null) {
            writer.writeAttribute("id", id);
        }
        try {
            if (archiveUnitRefId != null) {
                XMLWriterUtils.writeAttributeValue(writer, "ArchiveUnitRefId", archiveUnitRefId);
            }
        } catch (XMLStreamException e) {
            throw new VitamSedaException("Exception writing XML Stream", e);
        }
        if (archiveUnitProfile != null) {
            writeXMLFragment(archiveUnitProfile, writer);
        }
        if (rawManagementFile != null && rawManagementFile.exists()) {
            writeFile(rawManagementFile, writer);
        } else if (management != null) {
            writeXMLFragment(management, writer);
        }

        if (rawContentFile != null && rawContentFile.exists()) {
            writeFile(rawContentFile, writer);
        } else if (content != null) {
            for (DescriptiveMetadataContentType aContent : content) {
                writeXMLFragment(aContent, writer);
            }
        }
        if (archiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference != null) {

            for (Object obj : archiveUnitOrArchiveUnitReferenceAbstractOrDataObjectReference) {
                if (obj != null) {
                    if (obj instanceof DataObjectRefType) {
                        writeXMLFragment(obj, writer);
                    }
                    if (obj instanceof ArchiveUnitType) {
                        writeXMLFragment(new ArchiveUnitTypeRoot((ArchiveUnitType) obj), writer);
                    }
                }
            }
        }
        writer.writeEndElement();
    }

    /**
     * Serialize a Jaxb POJO object in the current XML stream
     * @param jaxbPOJO
     * @param writer
     * @throws VitamSedaException
     */
    private void writeXMLFragment(Object jaxbPOJO, XMLStreamWriter writer) throws VitamSedaException {
        try {
            MarshallerObjectCache.getMarshaller(jaxbPOJO.getClass()).marshal(jaxbPOJO, writer);
        } catch (JAXBException e) {
            throw new VitamSedaException("Error on writing " + jaxbPOJO + "object", e);
        }

    }

}
