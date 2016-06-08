package fr.gouv.culture.archivesdefrance.seda.v2;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * The override of the generated pojo is needed to describe it as a root element to generate the XML Stream
 */

@XmlRootElement(name="BinaryDataObject")
public class BinaryDataObjectTypeRoot extends BinaryDataObjectType {
    
}
