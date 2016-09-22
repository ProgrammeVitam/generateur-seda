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
package fr.gouv.vitam.generator.seda.module.invalid;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.scheduler.core.AbstractModule;
import fr.gouv.vitam.generator.scheduler.core.InputParameter;
import fr.gouv.vitam.generator.seda.api.SedaModuleParameter;

/**
 * 
 */
public class InvalidSizeModule extends AbstractModule implements PublicModuleInterface {
    private static final String MODULE_NAME = "InvalidSize";
    private static final Map<String,InputParameter> INPUTSIGNATURE = new HashMap<>();
    
    {
        INPUTSIGNATURE.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), new InputParameter().setObjectclass(BinaryDataObjectTypeRoot.class));
        INPUTSIGNATURE.put("file_regex", new InputParameter().setObjectclass(String.class));
        INPUTSIGNATURE.put("false_size", new InputParameter().setObjectclass(String.class).setMandatory(false).setDefaultValue("1"));
    }
    
    @Override
    public Map<String,InputParameter> getInputSignature(){
        return INPUTSIGNATURE;
    }
    
    @Override
    public String getModuleId() {
        return MODULE_NAME;
    }    
    
    @Override
    protected ParameterMap realExecute(ParameterMap parameters) throws VitamException {
        ParameterMap returnPM = new ParameterMap();
        Pattern p = Pattern.compile((String) parameters.get("file_regex"));
        BinaryDataObjectTypeRoot bdotr = (BinaryDataObjectTypeRoot) parameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName());
        if (p.matcher(bdotr.getFileInfo().getFilename()).matches()){
            BigInteger falseSize = new BigInteger((String)parameters.get("false_size"));
            bdotr.setSize(falseSize);
        }
        returnPM.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), bdotr);
        return returnPM;
    }

}
