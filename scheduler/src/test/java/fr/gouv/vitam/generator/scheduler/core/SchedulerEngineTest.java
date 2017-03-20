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

import static fr.gouv.vitam.generator.scheduler.api.TaskStatus.ABORT;
import static fr.gouv.vitam.generator.scheduler.api.TaskStatus.CONTINUE;
import static org.junit.Assert.fail;
import static org.mockito.BDDMockito.given;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.HashMap;
import java.util.Map;

import org.junit.Rule;
import org.junit.Test;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.scheduler.api.TaskInfo;

public class SchedulerEngineTest {

    @Rule
    public MockitoRule rule = MockitoJUnit.rule();

    @Test
    public void nominal() {
        ParameterMap input = new ParameterMap();
        input.put("mandatory_argument", "test");
        input.put("nullable_argument", null);
        launchPlaybook(input, "");
    }

    @Test
    public void should_skip_next_task_when_a_task_is_abort() throws VitamException {
        // Given
        ParameterMap input = new ParameterMap();

        PublicModuleInterface moduleContinue = mock(PublicModuleInterface.class);
        PublicModuleInterface moduleAbort = mock(PublicModuleInterface.class);

        Map<String, PublicModuleInterface> modules = new HashMap<>();
        modules.put("dummy", moduleContinue);
        modules.put("abort", moduleAbort);

        given(moduleContinue.execute(input)).willReturn(new TaskInfo(CONTINUE, input));
        given(moduleAbort.execute(input)).willReturn(new TaskInfo(ABORT, input));

        SchedulerEngine se = new SchedulerEngine(modules);
        Playbook playbook = new Playbook();
        playbook.getTasks().add(new Task("abort"));
        playbook.getTasks().add(new Task("dummy"));

        // When
        se.execute(playbook, input);

        // Then
        verify(moduleAbort).execute(any(ParameterMap.class));
        verify(moduleContinue, never()).execute(any(ParameterMap.class));
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

    private Playbook initiatePlaybook() {
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
        Playbook pb = initiatePlaybook();
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
