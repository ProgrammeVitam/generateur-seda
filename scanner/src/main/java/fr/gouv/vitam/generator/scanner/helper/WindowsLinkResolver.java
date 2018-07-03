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
package fr.gouv.vitam.generator.scanner.helper;

import static java.lang.String.format;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Set;

import com.google.common.base.Throwables;

import fr.gouv.vitam.generator.seda.exception.VitamBinaryDataObjectException;
import fr.gouv.vitam.generator.seda.helper.WindowsShortcut;

/**
 * Helper for windows link
 */
public class WindowsLinkResolver {

    public static final String WINDOWS_SHORTCUT_EXTENSION = ".lnk";

    public Path resolve(Path path) throws VitamBinaryDataObjectException {
        return resolve(path, new HashSet<>());
    }

    private Path resolve(Path path, Set<Path> pathAlreadyFollow) throws VitamBinaryDataObjectException {
        try {
            File file = new File(path.toString());
            WindowsShortcut windowsShortcut = new WindowsShortcut(file);
            Path target = Paths.get(windowsShortcut.getRealFilename());

            if (pathAlreadyFollow.contains(target)) {
                throw new VitamBinaryDataObjectException(format("link are circular, interruption: %s", target));
            }

            if (windowsLink(target)) {
                pathAlreadyFollow.add(target);
                return resolve(target, pathAlreadyFollow);
            }

            return target;

        } catch (IOException | ParseException e) {
            throw Throwables.propagate(e);
        }
    }

    public boolean windowsLink(Path path) {
        return path.toString().endsWith(WINDOWS_SHORTCUT_EXTENSION);
    }

}
