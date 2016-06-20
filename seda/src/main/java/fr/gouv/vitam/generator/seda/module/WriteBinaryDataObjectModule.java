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

import java.io.IOException;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.seda.api.SedaModuleParameter;
import fr.gouv.vitam.generator.seda.core.ArchiveTransferGenerator;
import fr.gouv.vitam.generator.seda.core.DataObjectGroupUsedMap;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;

/**
 * Write the binaryDataObject structure to XML and the binaryfile to the ZIP file<br>
 * Input: <br>
 * - binarydataobject (BinaryDataObjectTypeRoot) : the ready to write binaryDataObject Jaxb POJO<br> 
 * - dataobjectgroupID (String) : the dataobjectgroupID to which the binaryDataObject belongs<br>
 * - archivetransfergenerator (ArchiveTransferGenerator) : the object which has the pointer to XML and ZIP file<br>
 * Output:<br>
 * - binarydataobject (BinaryDataObjectTypeRoot)
 */

public class WriteBinaryDataObjectModule implements PublicModuleInterface {
    private static final String MODULE_NAME = "writeBinaryDataObject";

    @Override
    public String getModuleId() {
        return MODULE_NAME;
    }

    @Override
    public ParameterMap execute(ParameterMap parameters) throws VitamSedaException{
        ParametersChecker.checkParameter("parameters["+SedaModuleParameter.BINARYDATAOBJECT.getName()+"] cannot be null", parameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName()));
        // dataobjectgroupID can be null
        ParametersChecker.checkParameter("parameters[archivetransfergenerator] cannot be null", parameters.get("archivetransfergenerator"));
        BinaryDataObjectTypeRoot bdotr = (BinaryDataObjectTypeRoot) parameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName());
        ParameterMap returnPM = new ParameterMap();
        String dataObjectGroupID = (String) parameters.get("dataobjectgroupID");
        ArchiveTransferGenerator atgi = (ArchiveTransferGenerator) parameters.get("archivetransfergenerator");
        // DataObjectGroup Section
        // The given dogID is incorrect . we create a new ID
        DataObjectGroupUsedMap dogum = atgi.getDataObjectGroupUsedMap();

        if (dataObjectGroupID == null || (!dogum.existsDataObjectGroup(dataObjectGroupID))) {
            String newID = dogum.registerDataObjectGroup();
            bdotr.setDataObjectGroupId(newID);
            dogum.setUsedDataObjectGroup(newID);            
            // The given dogID has already be used
        } else if (dogum.isDataObjectGroupUsed(dataObjectGroupID)) {
            bdotr.setDataObjectGroupReferenceId(dataObjectGroupID);
            // It is a new dogID
        } else {
            bdotr.setDataObjectGroupId(dataObjectGroupID);
            dogum.setUsedDataObjectGroup(dataObjectGroupID);
        }

        // Write BinaryDataObject
        atgi.writeXMLFragment(bdotr);
        try{
            atgi.getZipFile().addFile("Content/"+bdotr.getId(), bdotr.getWorkingFilename());
        }catch(IOException e){
            throw new VitamSedaException("Error to write to zipFile",e);
        }
        returnPM.put(SedaModuleParameter.BINARYDATAOBJECT.getName(),bdotr);
        return returnPM;
    }

}