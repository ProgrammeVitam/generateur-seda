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
package fr.gouv.vitam.generator.seda.helper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * A class which helps to write a zip
 */
public class ZipFileWriter {
    private final ZipOutputStream zos;
    private static final int BUFFERSIZE = 64 * 1024;

    /**
     * Construct that initiate a ZipFile 
     * @param zipname : Name of the ZipName . Can be a name, a relative or a absolute Path
     * @throws FileNotFoundException
     */
    public ZipFileWriter(String zipname) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(zipname);
        this.zos = new ZipOutputStream(fos);

    }
    
    /**
     * Create a directory in the ZIP File
     * @param directory
     * @throws IOException
     */
    public void addDirectory(String directory) throws IOException{
        zos.putNextEntry(new ZipEntry(directory));
    }
    
    /**
     * Add a stream to the Zip 
     * @param filename : Name of the file in the Zip
     * @param is : Input Stream that will be added to the Zip
     * @throws IOException
     */
    public void addFile(String filename,InputStream is) throws IOException{
        // TODO nullity + file readability ?
        ZipEntry zipEntry = new ZipEntry(filename);

        zos.putNextEntry(zipEntry);

        byte[] bytes = new byte[BUFFERSIZE];
        int length;
        while ((length = is.read(bytes)) >= 0) {
            zos.write(bytes, 0, length);
        }

        zos.closeEntry();
    }
    
    /**
     * Add a File to the Zip
     * @param filename : name of the file in the zip 
     * @param fullpath : name of the file on the filesystem whose data will be added to the zip 
     * @throws IOException
     */
    public void addFile(String filename, String fullpath) throws IOException{
        //TODO use autoCloseable property try (allocation) { addFile }
        FileInputStream fis = new FileInputStream(fullpath);
        addFile(filename, fis);      
    }

    /**
     * Add a File to the zip
     * @param filename : name of the file inside and outside of the zip
     * @throws IOException
     */
    public void addFile(String filename) throws IOException{
        // Suppose que le filename est bien placé ? racine 
        addFile(filename, filename);      
    }

    /**
     * Close the zip file
     * @throws IOException
     */
    // Suggestion implement AutoCloseable: permet de faire un try(ZipFileWriter zfw = new ZFW()) {} avec close implicit
    public void closeZipFile() throws IOException{
        zos.flush();
        zos.close();
    } 

}
