/**
 * 
 */
package fr.gouv.culture.archivesdefrance.seda.v2;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.xml.datatype.XMLGregorianCalendar;

import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.helper.XMLWriterUtils;

/**
 * Helper for post-processing after the Json Unmarshalling
 *
 */
public class JsonMarshallerHelper {

    /**
     * Transform the RuleMap structure of Management or ManagementMetadata given by the unmarshalling of JSON to the JaxB structure
     * @param l
     */
    protected static void transformRuleMap(List l){
        if (l == null){
            return;
        }
        List l1= new ArrayList();
        for(int i=0;i<l.size();i++){
            Object rule = l.get(i);
            if (rule instanceof String){
                // This is a date 
                try{
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    Date date = formatter.parse((String) rule);
                    XMLGregorianCalendar xgc = XMLWriterUtils.getXMLGregorianCalendar(date);
                    l1.add(xgc);
                // This is a rule reference
                }catch(ParseException | VitamSedaException e){
                    RuleIdType rit = new RuleIdType();
                    rit.setValue((String) rule);
                    l1.add(rit);
                }
            }else if (rule instanceof LinkedHashMap){
                Map m = (Map<String,String>) rule;
                RuleIdType rit = new RuleIdType();
                rit.setId((String)m.get("Id"));
                rit.setValue((String)m.get("Value"));
                l1.add(rit);
            }
        }
        l.clear();
        l.addAll(l1);
    }
}
