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

import static fr.gouv.vitam.generator.scheduler.api.TaskStatus.ABORT;

import java.util.HashMap;
import java.util.Map;
import java.util.ServiceLoader;

import com.google.common.annotations.VisibleForTesting;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.scheduler.api.TaskInfo;
import fr.gouv.vitam.generator.scheduler.api.TaskStatus;

/**
 * SchedulerEngine
 */
public class SchedulerEngine {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(SchedulerEngine.class);

    private Map<String, PublicModuleInterface> modulesList;
    private static SchedulerMetrics sm = new SchedulerMetrics();

    /**
     * Enable the SPI discovery of the public modules
     */
    public SchedulerEngine() {
        modulesList = new HashMap<>();
        ServiceLoader<PublicModuleInterface> moduleLoader = ServiceLoader.load(PublicModuleInterface.class);
        for (PublicModuleInterface module : moduleLoader) {
            modulesList.put(module.getModuleId(), module);
            sm.registerModule(module.getModuleId());
        }
    }

    /**
     *
     * @param modulesList list of modules
     */
    @VisibleForTesting
    SchedulerEngine(Map<String, PublicModuleInterface> modulesList) {
        this.modulesList = modulesList;
        this.modulesList.forEach((key, value) -> sm.registerModule(key));
    }

    /**
     * Accessor to the list of public modules
     *
     * @return the list of public modules
     */
    public Map<String, PublicModuleInterface> getModulesList() {
        return modulesList;
    }

    /**
     * Execute a plugin
     *
     * @param playbook
     * @param initialParameters
     * @return the ParameterMaps of the playbook at the end of the execution
     * @throws VitamException
     */
    public ParameterMap execute(Playbook playbook, ParameterMap initialParameters) throws VitamException {
        for (Task task : playbook.getTasks()) {
            TaskStatus taskStatus = executeTask(task, initialParameters);
            if (taskStatus == ABORT) {
                LOGGER.debug("Task {} has abort status. The next tasks will be skipped", task.getModule());
                break;
            }
        }
        return initialParameters;
    }

    /**
     * Execute a task of a playbook
     *
     * @param task
     * @param globalParameters
     */
    private TaskStatus executeTask(Task task, ParameterMap globalParameters) throws VitamException {
        String moduleID = task.getModule();
        if (modulesList.containsKey(moduleID)) {
            long start = System.currentTimeMillis();
            ParameterMap sentParameters = task.substituteParameters(globalParameters);
            if(globalParameters.get("dataObjectGroupList") != null) {
                sentParameters.put("dataObjectGroupList", globalParameters.get("dataObjectGroupList"));
            }
            LOGGER.debug("Launch Task " + moduleID + " sent parameters :  " + sentParameters);
            TaskInfo taskInfo = modulesList.get(moduleID).execute(sentParameters);
            LOGGER.debug("Launch Task " + moduleID + " return parameters :  " + taskInfo);
            globalParameters.putAll(task.substituteRegisteredParameters(taskInfo.getParameterMap()));
            sm.addExecution(moduleID, System.currentTimeMillis() - start);
            return taskInfo.getStatus();
        } else {
            LOGGER.error(moduleID + " is Unknown. The task " + task.getName() + " will be skipped");
            return TaskStatus.CONTINUE;
        }
    }

    /**
     * Print the statistics of the Scheduler Engine
     */
    public void printStatistics() {
        sm.printStatistics();
    }

}
