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

import java.io.IOException;
import java.util.Locale;

import org.apache.http.Header;
import org.apache.http.HeaderIterator;
import org.apache.http.HttpEntity;
import org.apache.http.ProtocolVersion;
import org.apache.http.StatusLine;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.params.HttpParams;

/**
 * 
 */
public class CloseableHttpResponseMock implements CloseableHttpResponse {
    private HttpEntity httpEntity;
    /**
     * 
     */
    
    @Override
    public void setEntity(HttpEntity arg0) {
        httpEntity = arg0;

    }
    
    @Override
    public HttpEntity getEntity() {
        return httpEntity;
    }

    public CloseableHttpResponseMock() {
        // TODO Auto-generated constructor stub
    }

    @Override
    public Locale getLocale() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public StatusLine getStatusLine() {
        // TODO Auto-generated method stub
        return null;
    }



    @Override
    public void setLocale(Locale arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setReasonPhrase(String arg0) throws IllegalStateException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusCode(int arg0) throws IllegalStateException {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusLine(StatusLine arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusLine(ProtocolVersion arg0, int arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setStatusLine(ProtocolVersion arg0, int arg1, String arg2) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHeader(Header arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void addHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public boolean containsHeader(String arg0) {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public Header[] getAllHeaders() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Header getFirstHeader(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Header[] getHeaders(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Header getLastHeader(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HttpParams getParams() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public ProtocolVersion getProtocolVersion() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HeaderIterator headerIterator() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public HeaderIterator headerIterator(String arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void removeHeader(Header arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void removeHeaders(String arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeader(Header arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeader(String arg0, String arg1) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setHeaders(Header[] arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setParams(HttpParams arg0) {
        // TODO Auto-generated method stub

    }

    @Override
    public void close() throws IOException {
        // TODO Auto-generated method stub

    }

}
