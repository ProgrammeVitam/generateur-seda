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



import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import com.fasterxml.jackson.databind.JsonNode;

import fr.gouv.culture.archivesdefrance.seda.v2.BinaryDataObjectTypeRoot;
import fr.gouv.culture.archivesdefrance.seda.v2.FormatIdentificationType;
import fr.gouv.vitam.common.CharsetUtils;
import fr.gouv.vitam.common.ParametersChecker;
import fr.gouv.vitam.common.digest.DigestType;
import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.scheduler.core.AbstractModule;
import fr.gouv.vitam.generator.scheduler.core.InputParameter;
import fr.gouv.vitam.generator.seda.api.SedaModuleParameter;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;

/**
 * Module interacting with Siegfried (http://www.itforarchivists.com/siegfried)
 * <br>
 * Input :<br> 
 * - siegfriedURL (String) : URL to siegfried (without the /identify)<br>
 * - binarydataobject (BinaryDataObjectTypeRoot)<br>
 * Output<br>
 * - binarydataobject (BinaryDataObjectTypeRoot)
 */
public class SiegfriedModule extends AbstractModule implements PublicModuleInterface {
    // Only used to inject a Mock HTTP client in unit tests
    private CloseableHttpClient testhttpclient;
    private static final String MODULE_NAME = "siegfried";
    private static final Map<String,InputParameter> INPUTSIGNATURE = new HashMap<>();
    
    
    {
        INPUTSIGNATURE.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), new InputParameter().setObjectclass(BinaryDataObjectTypeRoot.class));
        INPUTSIGNATURE.put("siegfriedURL",new InputParameter().setObjectclass(String.class));

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
        BinaryDataObjectTypeRoot bdotr = (BinaryDataObjectTypeRoot) parameters.get(SedaModuleParameter.BINARYDATAOBJECT.getName());
        FormatIdentificationType format = bdotr.getFormatIdentification();
        File file = new File(bdotr.getWorkingFilename());
        try{
            String responseSiegfried = callSiegfried((String) parameters.get("siegfriedURL"), file);        
            JsonNode jsonNode = JsonHandler.getFromString(responseSiegfried);
            JsonNode j=jsonNode.get("files").get(0).get("matches").get(0);
            String mime = j.get("mime").asText();
            if (mime != null && mime.length() >0){
                format.setMimeType(mime);
            }
            String formatId = j.get("id").asText();
            if (formatId != null && formatId.length() > 0 ){
                format.setFormatId(formatId);
            }
            String formatLitteral=j.get("format").asText();
            if (formatLitteral != null && formatLitteral.length() >0){
                format.setFormatLitteral(formatLitteral);
            }
        
        } catch (InvalidParseOperationException e) {
            throw new VitamSedaException("Error on the Json got from Siegfried",e);
        } catch (IOException e) {
            throw new VitamSedaException("I/O error during Siegfried Module",e);
        }
        bdotr.setFormatIdentification(format);
        returnPM.put(SedaModuleParameter.BINARYDATAOBJECT.getName(), bdotr);
        return returnPM;
    }
        
    private String callSiegfried(String siegfriedURL , File file) throws VitamSedaException,IOException{
        CloseableHttpClient httpclient;
        String returnSiegfriedValue = null;
        if (testhttpclient != null){
            httpclient = testhttpclient;
        }else{
            httpclient = HttpClients.createDefault();
        }

        try{
            HttpPost post = new HttpPost(siegfriedURL + "/identify");
            MultipartEntityBuilder builder = MultipartEntityBuilder.create();
            builder.addBinaryBody("file", file);
            HttpEntity entity = builder.build();
            post.setEntity(entity);
            HttpResponse response = httpclient.execute(post);
            returnSiegfriedValue= EntityUtils.toString(response.getEntity(), CharsetUtils.UTF8);
        }catch(ClientProtocolException e){
            throw new VitamSedaException("Error on the HTTP client sent to siegfried",e);
        }finally{
            httpclient.close();
        }
        return returnSiegfriedValue;
    }
    
}
