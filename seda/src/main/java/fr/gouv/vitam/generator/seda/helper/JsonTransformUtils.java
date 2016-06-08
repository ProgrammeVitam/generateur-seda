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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stax.StAXResult;
import javax.xml.transform.stax.StAXSource;

import de.odysseus.staxon.json.JsonXMLConfig;
import de.odysseus.staxon.json.JsonXMLConfigBuilder;
import de.odysseus.staxon.json.JsonXMLInputFactory;

/**
 * 
 */
public class JsonTransformUtils {
    public static final String VIRTUALROOT_NAME="virtualroot";
    
    /**
     * Write the content of the json file to the XML Stream
     * @param writer : XML Stream on which the json file must be written
     * @param file : Json File that must be converted to XML 
     */
    
    public static void Json2XML(XMLStreamWriter writer,String file,String extractnode){
        InputStream input;
        try {
            ExtractStreamFilter filter=new ExtractStreamFilter(extractnode); 
            input = new FileInputStream(file);
            JsonXMLConfig config = new JsonXMLConfigBuilder().multiplePI(false).virtualRoot(VIRTUALROOT_NAME).build();
            XMLStreamReader reader = new JsonXMLInputFactory(config).createXMLStreamReader(input);
            reader = XMLInputFactory.newInstance().createFilteredReader(reader, filter);
            Source source = new StAXSource(reader);
            Result result = new StAXResult(new FragmentXMLStreamWriter(writer,VIRTUALROOT_NAME));
            TransformerFactory.newInstance().newTransformer().transform(source, result);
        }catch(FileNotFoundException e){
            e.printStackTrace();
        }catch(XMLStreamException e){
            e.printStackTrace();
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
