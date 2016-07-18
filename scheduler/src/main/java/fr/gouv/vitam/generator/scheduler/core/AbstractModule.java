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

import java.util.Map;
import java.util.Map.Entry;

import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.generator.scheduler.api.ModuleInterface;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;

/**
 * 
 */
public abstract class AbstractModule implements ModuleInterface { 
    @Override
    public abstract Map<String, InputParameter> getInputSignature();
    
    @Override
    public final ParameterMap execute(ParameterMap parameters) throws VitamException{
        checkParameters(parameters);
        return realExecute(parameters);
    }
    /**
     * Verify the "strong typing" of the Module
     * @param parameters
     */
    protected  void checkParameters(ParameterMap parameters) {
        Map<String, InputParameter> inputParameters = getInputSignature();
        for (Entry<String,InputParameter> e: inputParameters.entrySet()){ 
            String prefixException= "parameter["+e.getKey()+"] for module"+getModuleId();
            // Missing parameter . It the mandatory flag is present, exception, else we insert the default value
            if (!parameters.containsKey(e.getKey())){
                if (e.getValue().isMandatory()){
                    throw new IllegalArgumentException(prefixException+" is missing");
                }else{
                    parameters.put(e.getKey(), e.getValue().getDefaultValue());
                }
            }
            // Verify if the argument can be null
            if (!e.getValue().isNullable() && parameters.get(e.getKey()) == null){
                throw new IllegalArgumentException(prefixException+ " is null which is forbidden");
            }
            // Verify the type of the argument
            if (parameters.get(e.getKey())!= null &&   !e.getValue().getObjectclass().isInstance(parameters.get(e.getKey()))){
                throw new IllegalArgumentException(prefixException+ " is not of the class type" + e.getValue().getObjectclass().getCanonicalName());
            }
        }
    }
    
    protected abstract ParameterMap realExecute(ParameterMap parameters) throws VitamException;
    
}
