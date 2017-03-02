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
package fr.gouv.vitam.generator.scheduler.core;

import java.util.HashMap;

import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;

/**
 * A class to count the number and the time of call in the Scheduler Engine
 */

class SchedulerMetrics {
    private final HashMap<String, Long> nbCallModule;
    private final HashMap<String, Long> timeCallModule;
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(SchedulerMetrics.class);

    public SchedulerMetrics() {
        nbCallModule = new HashMap<>();
        timeCallModule = new HashMap<>();
    }


    /**
     * Register a module . It is made only once so that we prevent having a if condition in the addExecution method
     * @param moduleId
     */
    public void registerModule(String moduleId) {
        nbCallModule.put(moduleId, 0L);
        timeCallModule.put(moduleId, 0L);

    }

    /**
     * Register the metrics of an execution
     * @param moduleId : Module identification in the scenario
     * @param ms : time elapsed in ms
     */

    public void addExecution(String moduleId, long ms) {
        long nb = nbCallModule.get(moduleId);
        long time = timeCallModule.get(moduleId);
        nbCallModule.put(moduleId, nb + 1);
        timeCallModule.put(moduleId, time + ms);
    }

    /**
     * Publish the statistics
     */
    public void printStatistics() {
        LOGGER.info("Statistiques des modules de traitements");
        LOGGER.info("Module;Nb exec;Total time exec (ms); Mean Time (ms)");
        for (String moduleId : nbCallModule.keySet()) {//NOSONAR : We don't use entrySet as we iterate on 2 maps
            StringBuilder sb = new StringBuilder(moduleId).append(";").append(nbCallModule.get(moduleId)).append(";")
                .append(timeCallModule.get(moduleId)).append(";");
            if (nbCallModule.get(moduleId) > 0) {
                sb.append(timeCallModule.get(moduleId) / nbCallModule.get(moduleId));
            } else {
                sb.append("N/A");
            }
            LOGGER.info(sb.toString());
        }
    }
}
