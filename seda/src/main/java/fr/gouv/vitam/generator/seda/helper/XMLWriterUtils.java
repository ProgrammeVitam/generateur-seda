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
package fr.gouv.vitam.generator.seda.helper;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.concurrent.atomic.AtomicInteger;

import javax.xml.datatype.DatatypeConfigurationException;
import javax.xml.datatype.DatatypeFactory;
import javax.xml.datatype.XMLGregorianCalendar;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;

/**
 * Helper to manage XMLStreamWriter 
 */

public class XMLWriterUtils {

    private static AtomicInteger sequenceID = new AtomicInteger();

    private XMLWriterUtils() {
        // Empty constructor
    }

    /**
     * Write an attribute with only one value
     *
     * @param writer : The XMLStreamWriter on which the attribute is written
     * @param attribute
     * @param value
     * @throws XMLStreamException
     */
    public static void writeAttributeValue(XMLStreamWriter writer, String attribute, String value)
        throws XMLStreamException {
        // TODO null check ?
        writer.writeStartElement(attribute);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }

    /**
     * Get the next ID in the sequence
     * @return the next ID in the sequence
     */

    public static String getNextID() {
        sequenceID.incrementAndGet();
        return "ID" + sequenceID.get();
    }



    /**
     * Add to the current element an unique ID with the xml prefix (xml:id)
     *
     * @param writer : the current XMLStreamWrite
     * @return the ID of the added attribute
     * @throws XMLStreamException
     */

    public static String setID(XMLStreamWriter writer) throws XMLStreamException {
        return setID(writer, true);
    }

    /**
     * Add to the current element an unique ID
     * @param writer
     * @param prefix : attribute name is id if false, attribute name is xml:id if true
     * @return the ID of the ID attribute
     * @throws XMLStreamException
     */

    public static String setID(XMLStreamWriter writer, boolean prefix) throws XMLStreamException {
        String nextID = XMLWriterUtils.getNextID();
        if (prefix) {
            // TODO static final String ?
            writer.writeAttribute("xml", "xml", "id", nextID);
        } else {
            writer.writeAttribute("id", nextID);
        }
        return nextID;
    }

    /**
     * Get the current date in XML format
     *
     * @return a String which contains XML formated date of the current date
     */

    public static String getDate() {
        return XMLWriterUtils.getDate(null);
    }

    /**
     * Get the date in XML format
     *
     * @param date
     * @return a String which contains XML formated date of the given date
     * Suggestion: LocalDateTime (LocalDateUtil) possède déjà le défaut ISO
     */

    public static String getDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date d;
        if (date == null) {
            d = new Date();
        } else {
            d = date;
        }
        return sdf.format(d);
    }

    /**
     * Convert a Date to XMLGregorianCalendar Object
     * @param date
     * @return the XMLGregorianCalendar associated to the date parameter
     * Suggestion: LocalDateTime (LocalDateUtil)
     * @throws VitamSedaException
     * @throws IllegalArgumentException if the date parameter is null
     */
    public static XMLGregorianCalendar getXMLGregorianCalendar(Date date) throws VitamSedaException {
        ParametersChecker.checkParameter("date cannot be null", date);
        GregorianCalendar c = new GregorianCalendar();
        c.setTime(date);
        XMLGregorianCalendar xgc = null;
        try {
            xgc = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
        } catch (DatatypeConfigurationException e) {
            throw new VitamSedaException("Can't convert date " + date + " in XMLGregorianCalendar", e);
        }
        return xgc;
    }
}
