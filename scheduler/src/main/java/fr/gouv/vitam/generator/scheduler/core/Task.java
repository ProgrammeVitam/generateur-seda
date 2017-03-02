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

import com.fasterxml.jackson.annotation.JsonProperty;

import fr.gouv.vitam.generator.scheduler.api.ParameterMap;

/**
 * The task is the equivalent of the ansible task . It is the call of a module with input parameters and a list of
 * return parameters that must be registered to global variables
 */

public class Task {
    /**
     * Name of the task : it is a human-readable description of the task
     */
    private String name;
    /**
     * It is the module ID of the module called in this tasks
     */
    private String module;
    /**
     * Input Parameters of the tasks
     */
    @JsonProperty("parameters")
    private ParameterMap parameters;
    /**
     * Return Parameters that must be registered in the global parameters
     */
    @JsonProperty("registeredParameters")
    private ParameterMap registeredParameters;


    /**
     *
     * @return the parameters
     */
    public ParameterMap getParameters() {
        return parameters;
    }

    /**
     *
     * @return the binding parameters
     */
    public ParameterMap getRegisteredParameters() {
        return registeredParameters;
    }


    /**
     * @return the name
     */
    public String getName() {
        return name;
    }


    /**
     * @return the module
     */
    public String getModule() {
        return module;
    }


    /**
     * @param parameters the parameters to set
     *
     * @return this
     */
    public Task setParameters(ParameterMap parameters) {
        this.parameters = parameters;
        return this;
    }


    /**
     * @param registeredParameters the registeredParameters to set
     *
     * @return this
     */
    public Task setRegisteredParameters(ParameterMap registeredParameters) {
        this.registeredParameters = registeredParameters;
        return this;
    }


    /**
     * @param name the name to set
     *
     * @return this
     */
    public Task setName(String name) {
        this.name = name;
        return this;
    }


    /**
     * @param module the moduleID to set
     * @return this
     */
    public Task setModule(String module) {
        this.module = module;
        return this;
    }

}
