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

import static org.junit.Assert.fail;

import org.junit.Test;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;

public class SchedulerEngineTest {
    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(SchedulerEngineTest.class);

    @Test
    public void nominal() {
        ParameterMap input = new ParameterMap();
        input.put("mandatory_argument", "test");
        input.put("nullable_argument", null);
        launchPlaybook(input, "");
    }


    @Test
    public void missingMandatoryArgument() {
        ParameterMap input = new ParameterMap();
        input.put("nullable_argument", null);
        launchPlaybook(input, "parameter[mandatory_argument] for moduledummy is null which is forbidden");
    }

    @Test
    public void badClassArgument() {
        ParameterMap input = new ParameterMap();
        input.put("mandatory_argument", 1);
        launchPlaybook(input, "parameter[mandatory_argument] for moduledummy is not of the class typejava.lang.String");
    }



    private Playbook initatePlaybook() {
        Playbook pb = new Playbook();
        Task taskDummy = new Task();
        taskDummy.setName("DummyModule");
        taskDummy.setModule("dummy");
        ParameterMap pmDummy = new ParameterMap();
        pmDummy.put("mandatory_argument", "@@mandatory_argument@@");
        pmDummy.put("nullable_argument", "@@nullable_argument@@");
        taskDummy.setParameters(pmDummy);
        ParameterMap pmDummyRegistered = new ParameterMap();
        pmDummyRegistered.put("test", "@@test@@");
        taskDummy.setRegisteredParameters(pmDummyRegistered);
        pb.getTasks().add(taskDummy);
        return pb;
    }

    private void launchPlaybook(ParameterMap input, String expected) {
        SchedulerEngine se = new SchedulerEngine();
        Playbook pb = initatePlaybook();
        try {
            se.execute(pb, input);
            se.printStatistics();
        } catch (IllegalArgumentException e) {
            // It is the nominal behaviour of the test
            if (!(e.getMessage().equals(expected))) {
                fail(e.getMessage());
            }

        } catch (VitamException e) {
            fail(e.getMessage());
        }
    }

}
