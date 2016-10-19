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
import java.math.BigInteger;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.FileInfoType;
import fr.gouv.culture.archivesdefrance.seda.v2.FormatIdentificationType;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.scheduler.core.AbstractModule;
import fr.gouv.vitam.generator.scheduler.core.InputParameter;
import fr.gouv.vitam.generator.seda.api.SedaModuleParameter;
import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;
import fr.gouv.vitam.generator.seda.helper.XMLWriterUtils;

/**
 * Module : Constructor of the binaryDataObject
 * <br>
 * Input:<br> 
 *  - file (String) : Full path of the Binary object which will be the reference of the BinaryDataObject<br>
 * Output<br>
 *  - binarydataobject (BinaryDataObjectTypeRoot) : The built BinaryDataObject
 */

public class BinaryDataObjectConstructorModule extends AbstractModule implements PublicModuleInterface {
    private static final String MODULE_NAME = "binaryDataObjectConstructor";
    private static final Map<String,InputParameter> INPUTSIGNATURE = new HashMap<>();
    private static final Pattern DataObjectVersionFileName = Pattern.compile("^([a-zA-Z]*)_([0-9]+)_(.*)$");
    
    
    {
        INPUTSIGNATURE.put("file", new InputParameter().setObjectclass(String.class));
        INPUTSIGNATURE.put("dataobjectversion", new InputParameter().setObjectclass(String.class).setMandatory(false).setDefaultValue("BinaryMaster"));
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
    protected ParameterMap realExecute(ParameterMap parameters) throws VitamSedaException{
        ParameterMap returnPM = new ParameterMap();
        String id = XMLWriterUtils.getNextID();
        File f = new File((String)parameters.get("file"));
        if (!f.exists()){
            throw new VitamBinaryDataObjectException(f.getPath()+ "doesn't exist anymore");
        }
        if (!f.canRead()){
            throw new VitamBinaryDataObjectException(f.getPath()+ "is not readable");
        }
        if (f.length() == 0){
            throw new VitamBinaryDataObjectException(f.getPath()+" is an empty file");
        }
        BinaryDataObjectTypeRoot bdotr = new BinaryDataObjectTypeRoot();
        bdotr.setId(id);
        
        
        String extension = ".seda";
        int position = f.getName().lastIndexOf('.');
        if (position != -1){
            extension = f.getName().substring(position);
        }
        bdotr.setUri("Content/"+id+extension);
        FileInfoType fit = new FileInfoType();
        bdotr.setSize(new BigInteger(String.valueOf(f.length())));
        bdotr.setFormatIdentification(new FormatIdentificationType());
        fit.setLastModified(XMLWriterUtils.getXMLGregorianCalendar(new Date(f.lastModified())));
        // Match the format <Usage>_<Version>_<File Name>
        /*Matcher m = DataObjectVersionFileName.matcher(f.getName());
        if (m.matches()){
            bdotr.setDataObjectVersion(m.group(1)+"_"+m.group(2));
            fit.setFilename(m.group(3));
        }else{*/
            bdotr.setDataObjectVersion((String) parameters.get("dataobjectversion"));
            fit.setFilename(f.getName());
        //}
        bdotr.setFileInfo(fit);
        // Working attribute that is not put in the xml
        bdotr.setWorkingFilename(f.getPath());
        returnPM.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), bdotr);
        return returnPM;
    }
}