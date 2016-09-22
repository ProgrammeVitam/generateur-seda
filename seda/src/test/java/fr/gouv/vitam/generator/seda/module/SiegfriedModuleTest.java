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
package fr.gouv.vitam.generator.seda.module;

import static org.junit.Assert.fail;

import java.lang.reflect.Field;

import org.junit.Test;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.FormatIdentificationType;
import fr.gouv.vitam.common.exception.VitamException;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;

public class SiegfriedModuleTest {
    
    private static final String TESTFILE = SiegfriedModuleTest.class.getClassLoader().getResource("sip1.json").getFile();
    private static final String SIEGFRIED_OK = "{\"siegfried\":\"1.5.0\",\"scandate\":\"2016-06-17T12:42:15+02:00\",\"signature\":\"default.sig\",\"created\":\"2016-03-11T17:35:06+11:00\",\"identifiers\":[{\"name\":\"pronom\",\"details\":\"DROID_SignatureFile_V84.xml; container-signature-20160121.xml\"}],\"files\":[{\"filename\":\"a.sh\",\"filesize\": 8882,\"modified\":\"\",\"errors\": \"\",\"matches\": [{\"ns\":\"pronom\",\"id\":\"x-fmt/111\",\"format\":\"Plain Text File\",\"version\":\"\",\"mime\":\"text/plain\",\"basis\":\"text match UTF-8 Unicode\",\"warning\":\"match on text only; extension mismatch\"}]}]}";
    private static final String SIEGFRIED_NO_FORMAT_INFORMATION = "{\"siegfried\":\"1.5.0\",\"scandate\":\"2016-06-17T12:42:08+02:00\",\"signature\":\"default.sig\",\"created\":\"2016-03-11T17:35:06+11:00\",\"identifiers\":[{\"name\":\"pronom\",\"details\":\"DROID_SignatureFile_V84.xml; container-signature-20160121.xml\"}],\"files\":[{\"filename\":\"random\",\"filesize\": 10438,\"modified\":\"\",\"errors\": \"\",\"matches\": [{\"ns\":\"pronom\",\"id\":\"UNKNOWN\",\"format\":\"\",\"version\":\"\",\"mime\":\"\",\"basis\":\"\",\"warning\":\"no match\"}]}]}";
    
    
    @Test
    public void siegfriedOK() {
        mockSiegfried(SIEGFRIED_OK);
    }


    @Test
    public void siegfriedNoInformation() {
        mockSiegfried(SIEGFRIED_NO_FORMAT_INFORMATION);
    }
   
    
    private void mockSiegfried(String mockResponseValue){
        try {
            CloseableHttpclientMock mockHttpClient = new CloseableHttpclientMock();
            mockHttpClient.setResponseBody(mockResponseValue);
            ParameterMap pm = new ParameterMap();
            pm.put("siegfriedURL", "http://dummy");
            BinaryDataObjectTypeRoot bdotr= new BinaryDataObjectTypeRoot();
            bdotr.setFormatIdentification(new FormatIdentificationType());
            bdotr.setWorkingFilename(TESTFILE);
            pm.put("binarydataobject", bdotr);
            
            SiegfriedModule sm = new SiegfriedModule();
            Field f= sm.getClass().getDeclaredField("httpclient");
            f.setAccessible(true);
            f.set(sm, mockHttpClient);
            sm.execute(pm);
        } catch (NoSuchFieldException e) {
            fail("This should never happened as the testhttpclient exists");
        } catch (SecurityException|IllegalArgumentException|IllegalAccessException e) {
            e.printStackTrace();
            fail("This exception should not happened (linked to the java reflection on  SiegfriedModule)");
        } catch (VitamException e) {
            e.printStackTrace();
            fail("Real fail on SiegFriedModule");
        }
    }
}
