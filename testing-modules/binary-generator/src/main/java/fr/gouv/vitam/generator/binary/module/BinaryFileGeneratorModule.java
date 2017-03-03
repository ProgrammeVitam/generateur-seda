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


package fr.gouv.vitam.generator.binary.module;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

import fr.gouv.vitam.generator.scheduler.api.ParameterMap;
import fr.gouv.vitam.generator.scheduler.api.PublicModuleInterface;
import fr.gouv.vitam.generator.scheduler.core.AbstractModule;
import fr.gouv.vitam.generator.scheduler.core.InputParameter;
import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;
import fr.gouv.vitam.generator.seda.exception.VitamSedaException;

/**
 * Module : Generator for a binary object
 * <br>
 * Input:
 * <ul>
 *     <li>file (String) : Full path of the Binary object to generate</li>
 *     <li>size (Integer) : Size of the content to generate (in bytes)</li>
 * </ul>
 * Output: nothing
 *
 */
public class BinaryFileGeneratorModule extends AbstractModule implements PublicModuleInterface {
    private static final String MODULE_NAME = "binaryFileGenerator";
    private static final Map<String, InputParameter> INPUTSIGNATURE = new HashMap<>();
    private static final String SEPARATOR = "|";

    {
        INPUTSIGNATURE.put("file", new InputParameter().setObjectclass(String.class));
        INPUTSIGNATURE
            .put("size", new InputParameter().setObjectclass(Long.class).setMandatory(false).setDefaultValue(100));
    }

    @Override
    public Map<String, InputParameter> getInputSignature() {
        return INPUTSIGNATURE;
    }


    @Override
    public String getModuleId() {
        return MODULE_NAME;
    }

    @Override
    protected ParameterMap realExecute(final ParameterMap parameters) throws VitamSedaException {
        final File f = new File((String) parameters.get("file"));
        final Long length = (Long) parameters.get("size");

        try {
            if (!f.createNewFile()) {
                throw new VitamSedaException("Can't create temporary File" + f.getAbsolutePath());
            }
            // Write file content : concatenation of the file name
            long remainingToWrite = length;
            byte[] content = (f.getName() + SEPARATOR).getBytes(Charset.defaultCharset());
            try (OutputStream writer = new BufferedOutputStream(new FileOutputStream(f))) {
                // Write loop ; first as long as we can write everything fully
                while (remainingToWrite > content.length) {
                    writer.write(content);
                    remainingToWrite -= content.length;
                }
                // Cast to int safe here, as we now that it is <= content.length
                writer.write(content, 0, (int) remainingToWrite);
            }
        } catch (IOException e) {
            throw new VitamBinaryDataObjectException(f.getPath() + "doesn't exist anymore", e);
        }

        return new ParameterMap();
    }
}
