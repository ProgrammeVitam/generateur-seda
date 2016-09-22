package fr.gouv.vitam.generator.seda.core;

import java.io.File;
import java.nio.file.FileSystems;

import com.fasterxml.jackson.databind.JsonNode;

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
    public static final String CONFIG_NAME="ArchiveTransferConfig.json";
    /**
     * 
     * @param configDir
     * @param targetDir
     */
    public ArchiveTransferConfig(String configDir,String targetDir) {
        if (new File(configDir).isFile()){
            globalConfig = readJsonFile(configDir);
        }else{
            globalConfig = readJsonFile(configDir + FileSystems.getDefault().getSeparator() +  CONFIG_NAME); // NOSONAR : The default FileSystem must not be closed : https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close%28%29 
        }
        if (new File(targetDir).isFile()){
            scanConfig = readJsonFile(targetDir);
        }else{
            scanConfig = readJsonFile(targetDir + FileSystems.getDefault().getSeparator() +  CONFIG_NAME);// NOSONAR : The default FileSystem must not be closed : https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close%28%29 
        }
        
    }
    
    
    private JsonNode readJsonFile(String configFilePath) {
            JsonNode jn = JsonHandler.createObjectNode();
            try {
                jn = JsonHandler.getFromFile(new File(configFilePath));
            } catch (InvalidParseOperationException e) { // NOSONAR : presence of the file is optional
                LOGGER.debug("Parsing of config file "+ configFilePath + " impossible");
            }
            return jn;
    }

    /**
     * 
     * @param key
     * @return
     */
    public boolean has(String key){
        return globalConfig.has(key) ||scanConfig.has(key);
    }
    
    /**
     * 
     * @param key
     * @return
     */
    public JsonNode get(String key){
        if (scanConfig.has(key)){
            return scanConfig.get(key);
        }
        return globalConfig.get(key);
    }
}
