package fr.gouv.vitam.generator.scheduler.core;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.core.Playbook;
import fr.gouv.vitam.generator.scheduler.core.SchedulerEngine;
import fr.gouv.vitam.generator.scheduler.core.Task;

public class SchedulerEngineTest {
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(SchedulerEngineTest.class);
    
    @Test
    public void nominal() {
        ParameterMap input=new ParameterMap();
        input.put("mandatory_argument", "test");
        input.put("nullable_argument", null);
        launchPlaybook(input);
    }
    
    
    @Test
    public void missingMandatoryArgument() {
        ParameterMap input=new ParameterMap();
        input.put("nullable_argument", null);
        launchPlaybook(input);
    }
    
    @Test
    public void badClassArgument() {
        ParameterMap input=new ParameterMap();
        input.put("mandatory_argument", 1);
        launchPlaybook(input);
    }
    
    
    
    private Playbook initatePlaybook(){
        Playbook pb = new Playbook();
        Task taskDummy = new Task();
        taskDummy.setName("DummyModule");
        taskDummy.setModule("dummy");
        ParameterMap pmDummy = new ParameterMap();
        pmDummy.put("mandatory_argument","@@mandatory_argument@@");
        pmDummy.put("nullable_argument","@@nullable_argument@@");
        taskDummy.setParameters(pmDummy);
        ParameterMap pmDummyRegistered =new ParameterMap();
        pmDummyRegistered.put("test", "@@test");
        taskDummy.setRegisteredParameters(pmDummyRegistered);
        pb.getTasks().add(taskDummy);
        return pb;
    }
    
    private void launchPlaybook(ParameterMap input){
        SchedulerEngine se = new SchedulerEngine();
        Playbook pb = initatePlaybook();
        try{
            se.execute(pb,input);
            se.printStatistics();
        }catch(IllegalArgumentException e){
            // It is the nominal behaviour of the test
            LOGGER.info(e.getMessage());
        }catch(VitamException e){
            fail(e.getMessage());
        }        
    }
    
}
