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

package fr.gouv.vitam.generator.seda.module;

import static fr.gouv.vitam.generator.scheduler.api.TaskStatus.CONTINUE;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.MessageDigestBinaryObjectType;
import fr.gouv.vitam.common.BaseXx;
import fr.gouv.vitam.common.digest.Digest;
import fr.gouv.vitam.common.digest.DigestType;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.scheduler.api.TaskInfo;
import fr.gouv.vitam.generator.scheduler.core.AbstractModule;
import fr.gouv.vitam.generator.scheduler.core.InputParameter;
import fr.gouv.vitam.generator.seda.api.SedaModuleParameter;
import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;

/**
 * Module that will calculate the Digest of the file 
 * Input 
 * - binarydataobject (BinaryDataObjectTypeRoot) : binarydataobject that must be handled  
 * - file.digest.algorithm (String) :Algorithm of the digest 
 * Output : 
 * - binarydataobject
 */
public class DigestModule extends AbstractModule implements PublicModuleInterface {
    private static final String MODULE_NAME = "digest";
    private static final Map<String, InputParameter> INPUTSIGNATURE = new HashMap<>();

    static {
        INPUTSIGNATURE.put(SedaModuleParameter.BINARYDATAOBJECT.getName(),
            new InputParameter().setObjectclass(BinaryDataObjectTypeRoot.class));
        INPUTSIGNATURE.put("digest.algorithm",
            new InputParameter().setObjectclass(String.class).setMandatory(false).setDefaultValue(DigestType.SHA512));
    }

    @Override
    public Map<String, InputParameter> getInputSignature() {
        return INPUTSIGNATURE;
    }


    @Override
    public String getModuleId() {
        return MODULE_NAME;
    }

    @Override
    protected TaskInfo realExecute(ParameterMap parameters) throws VitamSedaException {
        BinaryDataObjectTypeRoot bdotr =
            (BinaryDataObjectTypeRoot) parameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName());
        ParameterMap returnPM = new ParameterMap();
        File f = new File(bdotr.getWorkingFilename());
        try {
            DigestType digestType = DigestType.valueOf((String) parameters.get("digest.algorithm"));
            MessageDigestBinaryObjectType mdbot = new MessageDigestBinaryObjectType();
            mdbot.setAlgorithm(digestType.getName());
            String base16Digest = BaseXx.getBase16(Digest.digest(f, digestType).digest());
            mdbot.setValue(base16Digest);
            bdotr.setMessageDigest(mdbot);
        } catch (IOException e) {
            throw new VitamBinaryDataObjectException(f.toString() + " has had an I/O exception" + e.getMessage(), e);
        }
        returnPM.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), bdotr);
        return new TaskInfo(CONTINUE, returnPM);
    }

}
