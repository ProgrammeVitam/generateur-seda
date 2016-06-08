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

import javax.xml.namespace.NamespaceContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

/**
 *  XMLStreamWriter implementation that avoid to write the StartDocument (<?xml) and the virtualroot that is a virtual object 
 *  added for JSON conversion
 */
public class FragmentXMLStreamWriter implements XMLStreamWriter {
    private XMLStreamWriter xsw;
    /**
     * Name of the virtualroot that will not be printed during the startElement
     */
    private String virtualroot;
    /**
     * The level of the current element that is processed . It is used to not avoid to use writeEndElement on the virtualroot . level 0 => virtualroot
     */
    private int level;

    public FragmentXMLStreamWriter(XMLStreamWriter xsw,String virtualroot){
        this.xsw = xsw;
        this.virtualroot = virtualroot;
        this.level=0;
    }
    
    @Override
    public void close() throws XMLStreamException {
        // Do nothing as the XMLStreamWriter will be used further

    }

    @Override
    public void writeStartDocument() throws XMLStreamException {
        // Do nothing as we don't want the <?xml> first line
    }

    @Override
    public void writeStartDocument(String version) throws XMLStreamException {
        // Do nothing as we don't want the <?xml> first line
    }

    @Override
    public void writeStartDocument(String encoding, String version) throws XMLStreamException {
        // Do nothing as we don't want the <?xml> first line
    }

    @Override
    public void writeEndDocument() throws XMLStreamException {
        // Do nothing as we don't want to flush the stacked elements
    }


    @Override
    public void writeStartElement(String localName) throws XMLStreamException {
        if (!localName.equals(virtualroot)){
            xsw.writeStartElement(localName);
            level+=1;
       }
    }

    @Override
    public void writeStartElement(String namespaceURI, String localName) throws XMLStreamException {
        if (!localName.equals(virtualroot)){
            xsw.writeStartElement(namespaceURI, localName);
            level+=1;
        }
    }

    @Override
    public void writeStartElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        if (!localName.equals(virtualroot)){
            xsw.writeStartElement(prefix, localName, namespaceURI);
            level+=1;
        }
    }


    @Override
    public void writeEndElement() throws XMLStreamException {
        if (level > 0){
            xsw.writeEndElement();
        }
        level-=1;
    }

    
    /* Vanilla methods up to the end of the class */
    
    @Override
    public void flush() throws XMLStreamException {
       xsw.flush();
    }
    
    @Override
    public NamespaceContext getNamespaceContext() {
        return xsw.getNamespaceContext();
    }

    @Override
    public String getPrefix(String uri) throws XMLStreamException {
        return xsw.getPrefix(uri);
    }

    @Override
    public Object getProperty(String name) {
        return xsw.getProperty(name);
    }

    @Override
    public void setDefaultNamespace(String uri) throws XMLStreamException {
        xsw.setDefaultNamespace(uri);
    }

    @Override
    public void setNamespaceContext(NamespaceContext context) throws XMLStreamException {
        xsw.setNamespaceContext(context);
    }

    @Override
    public void setPrefix(String prefix, String uri) throws XMLStreamException {
        xsw.setPrefix(prefix, uri);

    }

    @Override
    public void writeAttribute(String localName, String value) throws XMLStreamException {
        xsw.writeAttribute(localName, value);

    }

    @Override
    public void writeAttribute(String namespaceURI, String localName, String value) throws XMLStreamException {
        xsw.writeAttribute(namespaceURI, localName, value);

    }

    @Override
    public void writeAttribute(String prefix, String namespaceURI, String localName, String value)
        throws XMLStreamException {
        xsw.writeAttribute(prefix, namespaceURI, localName, value);
    }

    @Override
    public void writeCData(String data) throws XMLStreamException {
        xsw.writeCData(data);

    }

    @Override
    public void writeCharacters(String text) throws XMLStreamException {
        xsw.writeCharacters(text);
    }

    @Override
    public void writeCharacters(char[] text, int start, int len) throws XMLStreamException {
        xsw.writeCharacters(text, start, len);
    }

    @Override
    public void writeComment(String data) throws XMLStreamException {
        xsw.writeComment(data);
    }

    @Override
    public void writeDTD(String dtd) throws XMLStreamException {
        xsw.writeDTD(dtd);
    }

    @Override
    public void writeDefaultNamespace(String namespaceURI) throws XMLStreamException {
        xsw.writeDefaultNamespace(namespaceURI);
    }

    @Override
    public void writeEmptyElement(String localName) throws XMLStreamException {
        xsw.writeEmptyElement(localName);
    }

    @Override
    public void writeEmptyElement(String namespaceURI, String localName) throws XMLStreamException {
        xsw.writeEmptyElement(namespaceURI, localName);
    }

    @Override
    public void writeEmptyElement(String prefix, String localName, String namespaceURI) throws XMLStreamException {
        xsw.writeEmptyElement(prefix, localName, namespaceURI);

    }


    @Override
    public void writeEntityRef(String name) throws XMLStreamException {
        xsw.writeEntityRef(name);

    }

    @Override
    public void writeNamespace(String prefix, String namespaceURI) throws XMLStreamException {
        xsw.writeNamespace(prefix, namespaceURI);

    }

    @Override
    public void writeProcessingInstruction(String target) throws XMLStreamException {
        xsw.writeProcessingInstruction(target);
    }

    @Override
    public void writeProcessingInstruction(String target, String data) throws XMLStreamException {
        xsw.writeProcessingInstruction(target, data);

    }

}