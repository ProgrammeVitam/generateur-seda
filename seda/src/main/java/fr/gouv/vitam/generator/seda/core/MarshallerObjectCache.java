package fr.gouv.vitam.generator.seda.core;

import java.util.HashMap;
import java.util.Map;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;

/**
 * Cache the Marshaller Object as its initialization takes about 40ms 
 */
class MarshallerObjectCache {
    private static Map<Class<?>,Marshaller> marshallbyclass = new HashMap<>();

    private MarshallerObjectCache(){
        // Empty constructor
    }
    
    /**
     * Cache of the marshaller object
     * @param c : class whom we want the JAXB Marshaller
     * @return The JAXB Marshaller for the class given in argument
     * @throws JAXBException
     */
    
    public static Marshaller getMarshaller(Class<?> c) throws JAXBException{
        if (marshallbyclass.get(c) == null){
            JAXBContext jc = JAXBContext.newInstance(c);
            Marshaller marshaller = jc.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FRAGMENT, true);
            marshallbyclass.put(c, marshaller);
        }
        return marshallbyclass.get(c);
        
    }
    

}
