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

import java.io.File;
import java.io.IOException;
import java.util.Base64;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.MessageDigestBinaryObjectType;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.digest.Digest;
import fr.gouv.vitam.common.digest.DigestType;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
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
public class DigestModule implements PublicModuleInterface {
    private static final String MODULE_NAME = "digest";

    @Override
    public String getModuleId() {
        return MODULE_NAME;
    }

    @Override
    public ParameterMap execute(ParameterMap parameters) throws VitamSedaException{
        ParametersChecker.checkParameter("parameters["+SedaModuleParameter.BINARYDATAOBJECT.getName()+"] cannot be null", parameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName()));
        ParametersChecker.checkParameter("parameters[digest.algorithm] cannot be null", parameters.get("digest.algorithm"));
        BinaryDataObjectTypeRoot bdotr = (BinaryDataObjectTypeRoot) parameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName());
        ParameterMap returnPM = new ParameterMap();
        File f = new File(bdotr.getWorkingFilename());
        try {
            DigestType digestType = DigestType.valueOf((String)parameters.get("digest.algorithm"));
            MessageDigestBinaryObjectType mdbot = new MessageDigestBinaryObjectType();
            mdbot.setAlgorithm(digestType.getName());
            // BaseXx contient Base64
            mdbot.setValue(Base64.getEncoder().encodeToString(Digest.digest(f, digestType).digest()));
            // Hexa serait plus correct, non ? fonction native de Digest et supporté par SEDA (et par Vitam du coup)
            bdotr.setMessageDigest(mdbot);
        } catch (IOException e) {
            throw new VitamBinaryDataObjectException("IO Exception on the file" + f.toString(), e);
        }
        returnPM.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), bdotr);
        return returnPM;
    }
}
