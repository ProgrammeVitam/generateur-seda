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
package fr.gouv.vitam.generator.scanner.core;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.xml.stream.XMLStreamException;

import fr.gouv.vitam.common.exception.VitamException;


/**
 * TODO
 */
// TODO implements AutoCloseable
public class ScanFileSystemTree {

    // TODO all final
    private String baseDir;
    private String configFile;
    private String outputFile;
    private String playbookFile;
    /**
     * Default Constructor 
     * @param baseDir : root of the path that has to be scanned
     * @param configFile : JSON file containing the global parameter of the ArchiveTransferRequest 
     * @param playbookFile
     * @param outputFile : xml outputfile
     */

    public ScanFileSystemTree(String baseDir, String configFile,String playbookFile, String outputFile) {
        // null check ?
        this.baseDir = baseDir;
        this.configFile = configFile;
        this.outputFile = outputFile;
        this.playbookFile = playbookFile;
    }
    /**
     * Scan the filesystem
     */
    public void scan() throws IOException, XMLStreamException, VitamException {
        FileSystem  f = FileSystems.getDefault(); //NOSONAR : The default FileSystem must not be closed : https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html#close%28%29
        Path p = f.getPath(baseDir);
        ScanFS sfs = new ScanFS(configFile, playbookFile,outputFile);
        Files.walkFileTree(p, sfs);
        // TODO in close of AutoCloseable
        sfs.endScan();
    }

}
