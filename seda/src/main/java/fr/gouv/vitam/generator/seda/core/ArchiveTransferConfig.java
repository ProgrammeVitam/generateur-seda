/**
 * Copyright French Prime minister Office/SGMAP/DINSIC/Vitam Program (2015-2019)
 * <p>
 * contact.vitam@culture.gouv.fr
 * <p>
 * This software is a computer program whose purpose is to implement a digital
 * archiving back-office system managing high volumetry securely and efficiently.
 * <p>
 * This software is governed by the CeCILL 2.1 license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL 2.1
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 * <p>
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 * <p>
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
 * <p>
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL 2.1 license and that you accept its terms.
 */
package fr.gouv.vitam.generator.seda.core;

import java.io.File;
import java.nio.file.FileSystems;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;

import fr.gouv.vitam.common.exception.InvalidParseOperationException;
import fr.gouv.vitam.common.json.JsonHandler;
import fr.gouv.vitam.common.logging.VitamLogger;
import fr.gouv.vitam.common.logging.VitamLoggerFactory;

/**
 *
 */
public class ArchiveTransferConfig {

    private static final VitamLogger LOGGER = VitamLoggerFactory.getInstance(ArchiveTransferConfig.class);

    private JsonNode globalConfig;
    private JsonNode scanConfig;
    public static final String CONFIG_NAME = "ArchiveTransferConfig.json";

    /**
     * @param configDir
     * @param targetDir
     */
    public ArchiveTransferConfig(String configDir, String targetDir) {
        if (new File(configDir).isFile()) {
            globalConfig = readJsonFile(configDir);
        } else {
            globalConfig = readJsonFile(configDir + FileSystems.getDefault().getSeparator() +
                CONFIG_NAME); // NOSONAR : The default FileSystem must not be closed : https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close%28%29
        }
        if (targetDir != null && new File(targetDir).isFile()) {
            scanConfig = readJsonFile(targetDir);
        } else {
            scanConfig = readJsonFile(targetDir + FileSystems.getDefault().getSeparator() +
                CONFIG_NAME);// NOSONAR : The default FileSystem must not be closed : https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close%28%29
        }

    }


    private JsonNode readJsonFile(String configFilePath) {
        JsonNode jn = JsonHandler.createObjectNode();
        try {
            jn = JsonHandler.getFromFile(new File(configFilePath));
        } catch (InvalidParseOperationException e) { // NOSONAR : presence of the file is optional
            LOGGER.debug("Parsing of config file " + configFilePath + " impossible");
        }
        return jn;
    }

    /**
     * @param key
     * @return
     */
    public boolean has(String key) {
        return globalConfig.has(key) || scanConfig.has(key);
    }

    /**
     * @param key
     * @return the JsonNode below the first level key given
     */
    public JsonNode get(String key) {
        if (scanConfig.has(key)) {
            return scanConfig.get(key);
        }
        return globalConfig.get(key);
    }

    private void set(String key, String value) {
        if (scanConfig != null) {
            ((ObjectNode) scanConfig).put(key, value);
        } else {
            ((ObjectNode) globalConfig).put(key, value);
        }
    }

    /**
     * @param key
     * @return the String at the first level key given if this is a String else return an empty string
     */
    public String getString(String key) {
        if (has(key) && get(key).getNodeType().equals(JsonNodeType.STRING)) {
            return get(key).textValue();
        } else {
            return "";
        }
    }

    /**
     * Override the messageIdentifier
     *
     * @param messageId
     */
    public void setMessageIdentifer(String messageId) {
        set("MessageIdentifier", messageId);
    }
}
