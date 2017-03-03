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
 */
public class JsonMarshallerHelper {

    /**
     * Transform the RuleMap structure of Management or ManagementMetadata given by the unmarshalling of JSON to the JaxB structure
     *
     * @param l
     */
    protected static void transformRuleMap(List l) {
        if (l == null) {
            return;
        }
        List l1 = new ArrayList();
        for (int i = 0; i < l.size(); i++) {
            Object rule = l.get(i);
            if (rule instanceof String) {
                // This is a date 
                try {
                    DateFormat formatter = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
                    Date date = formatter.parse((String) rule);
                    XMLGregorianCalendar xgc = XMLWriterUtils.getXMLGregorianCalendar(date);
                    l1.add(xgc);
                    // This is a rule reference
                } catch (ParseException | VitamSedaException e) {// NOSONAR : don't rethrow as the exception is a method to define the type of the input
                    RuleIdType rit = new RuleIdType();
                    rit.setValue((String) rule);
                    l1.add(rit);
                }
            } else if (rule instanceof LinkedHashMap) {
                Map m = (Map<String, String>) rule;
                RuleIdType rit = new RuleIdType();
                rit.setId((String) m.get("Id"));
                rit.setValue((String) m.get("Value"));
                l1.add(rit);
            }
        }
        l.clear();
        l.addAll(l1);
    }
}
