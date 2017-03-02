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
package fr.gouv.vitam.generator.scheduler.core;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.ServiceLoader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;

/**
 *  SchedulerEngine
 */
public class SchedulerEngine {
    /*
     * TODO either choose static or not static
     * For instance:
     * - static: all static final and already allocated and build from a static { } step but not in constructor
     * - non static: all non static but final and then in constructor
     */
    private static Map<String, PublicModuleInterface> modulesList = null;
    private ServiceLoader<PublicModuleInterface> moduleLoader = ServiceLoader.load(PublicModuleInterface.class);
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(SchedulerEngine.class);
    private static SchedulerMetrics sm = new SchedulerMetrics();

    /**
     * Enable the SPI discovery of the public modules 
     */
    public SchedulerEngine() {
        if (modulesList == null) {
            modulesList = new HashMap<>();
            moduleLoader.reload();
            for (PublicModuleInterface module : moduleLoader) {
                modulesList.put(module.getModuleId(), module);
                sm.registerModule(module.getModuleId());
            }

        }
    }

    /**
     * Accessor to the list of public modules
     * @return the list of public modules
     */
    public Map<String, PublicModuleInterface> getModulesList() {
        return modulesList;
    }

    /**
     * Execute a plugin
     * @param playbook
     * @param initialParameters
     * @return the ParameterMaps of the playbook at the end of the execution 
     * @throws VitamException
     */
    public ParameterMap execute(Playbook playbook, ParameterMap initialParameters) throws VitamException {
        ParameterMap pm = initialParameters;
        for (Task t : playbook.getTasks()) {
            executeTask(t, pm);
        }
        return pm;
    }

    /**
     * Templating of the ParameterMap
     * @param templateParameters : a template parameter map with @@ @@ pattern 
     * @param valuesParameters : the values to valuate the template
     * @return the valuated ParameterMap
     */
    private ParameterMap substitute(ParameterMap templateParameters, ParameterMap valuesParameters) {
        ParameterMap pm = new ParameterMap();
        Pattern pattern = Pattern.compile("^@@(.*)@@$");
        for (Entry<String, Object> entry : templateParameters.entrySet()) {
            String value = (String) entry.getValue();
            Matcher matcher = pattern.matcher(value);
            if (matcher.find()) {
                pm.put(entry.getKey(), valuesParameters.get(matcher.group(1)));
            } else {
                pm.put(entry.getKey(), value);
            }
        }
        return pm;
    }

    /**
     * Execute a task of a playbook
     * @param task
     * @param globalParameters
     */
    private void executeTask(Task task, ParameterMap globalParameters) throws VitamException {
        ParameterMap sentParameters;
        ParameterMap returnTaskParameters;
        String moduleID = task.getModule();
        if (modulesList.containsKey(moduleID)) {
            long start = System.currentTimeMillis();
            sentParameters = substitute(task.getParameters(), globalParameters);
            LOGGER.debug("Launch Task " + moduleID + " sent parameters :  " + sentParameters);
            returnTaskParameters = modulesList.get(moduleID).execute(sentParameters);
            LOGGER.debug("Launch Task " + moduleID + " return parameters :  " + returnTaskParameters);
            globalParameters.putAll(substitute(task.getRegisteredParameters(), returnTaskParameters));
            sm.addExecution(moduleID, System.currentTimeMillis() - start);
        } else {
            LOGGER.error(moduleID + " is Unknown. The task " + task.getName() + " will be skipped");
        }
    }

    /**
     * Print the statistics of the Scheduler Engine
     */
    public void printStatistics() {
        sm.printStatistics();
    }

}
