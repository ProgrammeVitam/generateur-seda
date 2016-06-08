package fr.gouv.vitam.generator.scheduler.core;

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.core.Playbook;
import fr.gouv.vitam.generator.scheduler.core.SchedulerEngine;
import fr.gouv.vitam.generator.scheduler.core.Task;

public class SchedulerEngineTest {

    @Test
    public void test() {
        Playbook pb = new Playbook();
        Task taskDummy = new Task();
        taskDummy.setName("DummyModule");
        taskDummy.setModule("dummy");
        ParameterMap pmDummy = new ParameterMap();
        pmDummy.put("file","@@binarydataobjectfile@@");
        taskDummy.setParameters(pmDummy);
        ParameterMap pmDummyRegistered =new ParameterMap();
        pmDummyRegistered.put("test", "@@test");
        taskDummy.setRegisteredParameters(pmDummyRegistered);
        pb.getTasks().add(taskDummy);
        SchedulerEngine se = new SchedulerEngine();
        ParameterMap initial=new ParameterMap();
        initial.put("file","test");
        // Variable which is not in the input parameter of the module
        initial.put("file1","test");
        try{
            ParameterMap fin=se.execute(pb,initial);
            se.printStatistics();
        }catch(VitamException e){
            e.printStackTrace();
            fail(e.getMessage());
        }        
  
    }
}
