package fr.gouv.vitam.generator.scheduler.core;

import java.util.HashMap;

import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;

/**
 * A class to count the number and the time of call in the Scheduler Engine
 */

class SchedulerMetrics {
    private final HashMap<String,Long> nbCallModule;
    private final HashMap<String,Long> timeCallModule;
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(SchedulerMetrics.class);

    public SchedulerMetrics() {
        nbCallModule= new HashMap<>();
        timeCallModule = new HashMap<>();
    }
    

    /**
     * Register a module . It is made only once so that we prevent having a if condition in the addExecution method
     * @param moduleId
     */
    public void registerModule(String moduleId){
        nbCallModule.put(moduleId, 0L);
        timeCallModule.put(moduleId, 0L);
        
    }
    
    /**
     * Register the metrics of an execution
     * @param moduleId : Module identification in the scenario
     * @param ms : time elapsed in ms
     */
    
    public void addExecution(String moduleId, long ms){
        long nb = nbCallModule.get(moduleId);
        long time = timeCallModule.get(moduleId);
        nbCallModule.put(moduleId, nb+1);
        timeCallModule.put(moduleId, time+ms);
    }
    
    /**
     * Publish the statistics
     */
    public void printStatistics(){
        LOGGER.debug("Module;Nb exec;Total time exec (ms); Mean Time (ms)");
        for (String moduleId : nbCallModule.keySet()){//NOSONAR : We don't use entrySet as we iterate on 2 maps
            StringBuilder sb = new StringBuilder(moduleId).append(";").append(nbCallModule.get(moduleId)).append(";").append(timeCallModule.get(moduleId)).append(";");
            if (nbCallModule.get(moduleId)>0){
                sb.append(timeCallModule.get(moduleId)/nbCallModule.get(moduleId));
            }else{
                sb.append("N/A");
            }
            LOGGER.info(sb.toString());
        }
    }
}
