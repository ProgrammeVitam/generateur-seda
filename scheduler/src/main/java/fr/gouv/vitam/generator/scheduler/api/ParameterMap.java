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

package fr.gouv.vitam.generator.scheduler.api;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Definition of a Type Alias for the parameters passed and returned by the execution of tasks
 */
public class ParameterMap extends HashMap<String, Object> {
    private static final long serialVersionUID = -8501319445037778743L;

    private final static Pattern PATTERN = Pattern.compile("^@@(.*)@@$");


    /**
     * Templating of the ParameterMap
     *
     * @param valuesParameters : the values to valuate the template
     * @return the valuated ParameterMap
     */
    public ParameterMap substitute(ParameterMap valuesParameters) {
        ParameterMap pm = new ParameterMap();
        for (Entry<String, Object> entry : entrySet()) {
            String value = (String) entry.getValue();
            Matcher matcher = PATTERN.matcher(value);
            if (matcher.find()) {
                pm.put(entry.getKey(), valuesParameters.get(matcher.group(1)));
            } else {
                pm.put(entry.getKey(), value);
            }
        }
        return pm;
    }

}
