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
package fr.gouv.vitam.generator.seda.helper;

import java.text.SimpleDateFormat;
import java.util.Date;

import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

public class XMLWriterUtils {

    private static int sequenceID = 0;
    
    /**
     * Write an attribute with only one value
     * @param writer : The XMLStreamWriter on which the attribute is written
     * @param attribute
     * @param value
     * @throws XMLStreamException
     */
    public static void writeAttributeValue(XMLStreamWriter writer,String attribute,String value) throws XMLStreamException {
        writer.writeStartElement(attribute);
        writer.writeCharacters(value);
        writer.writeEndElement();
    }
    

    public static String getNextID(){
        sequenceID +=1;
        return "ID"+sequenceID;
    }

    
    
    /**
     * Add to the current element an unique ID with the xml prefix (xml:id)
     * @param writer : the current XMLStreamWrite
     * @return
     * @throws XMLStreamException
     */
    
    public static String setID(XMLStreamWriter writer) throws XMLStreamException{
        return setID(writer,true);
    }
    
    
    public static String setID(XMLStreamWriter writer,boolean prefix) throws XMLStreamException{
        String nextID = XMLWriterUtils.getNextID();
        if (prefix){
            writer.writeAttribute("xml","xml","id",nextID);
        }else{
            writer.writeAttribute("id", nextID);
        }
        return nextID;
    }
    
    /**
     * Get the current date in XML format
     * @return a String which contains XML formated date of the current date
     */
    
    public static String getDate(){
        return XMLWriterUtils.getDate(null);
    }

    /**
     * Get the date in XML format
     * @param date
     * @return a String which contains XML formated date of the given date
     */
    
    public static String getDate(Date date){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
        Date d;
        if (date == null){
            d = new Date();
        }else{
            d = date;
        }
        return sdf.format(d);
    }
}
