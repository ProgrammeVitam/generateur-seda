package fr.gouv.vitam.generator.seda.module.invalid;

import static org.junit.Assert.fail;

import java.math.BigInteger;

import org.junit.Test;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.FileInfoType;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.seda.api.SedaModuleParameter;

public class InvalidSizeModuleTest {
    private static final String TESTFILE = "sip1.json";
    private static final long REALSIZE = 1337;
    private static final String FALSESIZE = "10";

    @Test
    public void invalidSizeMatchingFile() {
        ParameterMap pm = new ParameterMap();
        BinaryDataObjectTypeRoot bdotr = new BinaryDataObjectTypeRoot();
        FileInfoType fit = new FileInfoType();
        fit.setFilename(TESTFILE);
        bdotr.setFileInfo(fit);
        bdotr.setSize(BigInteger.valueOf(REALSIZE));
        pm.put("file_regex", "^.*sip.*json");
        pm.put("false_size", FALSESIZE);
        pm.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), bdotr);
        InvalidSizeModule ism = new InvalidSizeModule();
        try{
            ParameterMap returnParameters = ism.execute(pm);
            bdotr= (BinaryDataObjectTypeRoot) returnParameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName());
            if (!bdotr.getSize().toString().equals(FALSESIZE)){
                fail("The size has not been changed ");
            }
            
        }catch(VitamException e){
            fail(e.getMessage());
        }
        
    }
   
    @Test
    public void invalidSizeNotMatchingFile() {
        ParameterMap pm = new ParameterMap();
        BinaryDataObjectTypeRoot bdotr = new BinaryDataObjectTypeRoot();
        FileInfoType fit = new FileInfoType();
        fit.setFilename(TESTFILE);
        bdotr.setFileInfo(fit);
        bdotr.setSize(BigInteger.valueOf(REALSIZE));
        pm.put("file_regex", "NOTMATCHING.*json");
        pm.put("false_size", FALSESIZE);
        pm.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), bdotr);
        InvalidSizeModule ism = new InvalidSizeModule();
        try{
            ParameterMap returnParameters = ism.execute(pm);
            bdotr= (BinaryDataObjectTypeRoot) returnParameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName());
            if (bdotr.getSize().toString().equals(FALSESIZE)){
                fail("The size has been changed but the file is not matching the regex");
            }
            
        }catch(VitamException e){
            fail(e.getMessage());
        }
        
    }
    
    
}
