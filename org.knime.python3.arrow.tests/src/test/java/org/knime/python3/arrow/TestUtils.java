/*
 * ------------------------------------------------------------------------
 *
 *  Copyright by KNIME AG, Zurich, Switzerland
 *  Website: http://www.knime.com; Email: contact@knime.com
 *
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License, Version 3, as
 *  published by the Free Software Foundation.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, see <http://www.gnu.org/licenses>.
 *
 *  Additional permission under GNU GPL version 3 section 7:
 *
 *  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
 *  Hence, KNIME and ECLIPSE are both independent programs and are not
 *  derived from each other. Should, however, the interpretation of the
 *  GNU GPL Version 3 ("License") under any applicable laws result in
 *  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
 *  you the additional permission to use and propagate KNIME together with
 *  ECLIPSE with only the license terms in place for ECLIPSE applying to
 *  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
 *  license terms of ECLIPSE themselves allow for the respective use and
 *  propagation of ECLIPSE together with KNIME.
 *
 *  Additional permission relating to nodes for KNIME that extend the Node
 *  Extension (and in particular that are based on subclasses of NodeModel,
 *  NodeDialog, and NodeView) and that only interoperate with KNIME through
 *  standard APIs ("Nodes"):
 *  Nodes are deemed to be separate and independent programs and to not be
 *  covered works.  Notwithstanding anything to the contrary in the
 *  License, the License does not apply to Nodes, you are not required to
 *  license Nodes under the License, and you are granted a license to
 *  prepare and propagate Nodes, in each case even if such Nodes are
 *  propagated with or for interoperation with KNIME.  The owner of a Node
 *  may freely choose the license terms applicable to such Node, including
 *  when such Node is propagated with or for interoperation with KNIME.
 * ---------------------------------------------------------------------
 *
 * History
 *   Apr 12, 2021 (benjamin): created
 */
package org.knime.python3.arrow;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.apache.commons.lang3.SystemUtils;
import org.knime.python3.PythonCommand;
import org.knime.python3.PythonDataSink;
import org.knime.python3.PythonDataSource;
import org.knime.python3.PythonEntryPoint;
import org.knime.python3.PythonExtension;
import org.knime.python3.PythonGateway;
import org.knime.python3.PythonModuleKnimeGateway;
import org.knime.python3.PythonPath;
import org.knime.python3.PythonPath.PythonPathBuilder;
import org.knime.python3.SimplePythonCommand;

/**
 * Utilities for Python Arrow data transfer tests.
 *
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
public final class TestUtils {

    private static final String PYTHON_EXE_ENV = "PYTHON3_EXEC_PATH";

    private TestUtils() {
        // Static utility class
    }

    /** Create a Python command from the path in the env var PYTHON3_EXEC_PATH */
    private static PythonCommand getPythonCommand() throws IOException {
        final String python3path = System.getenv(PYTHON_EXE_ENV);
        if (python3path != null) {
            return new SimplePythonCommand(python3path);
        }
        throw new IOException(
            "Please set the environment variable '" + PYTHON_EXE_ENV + "' to the path of the Python 3 executable.");
    }

    /**
     * Create a temporary file which is deleted on exit.
     *
     * @return the file
     * @throws IOException if the file could not be created
     */
    public static Path createTmpKNIMEArrowPath() throws IOException {
        final Path path = Files.createTempFile("KNIME-" + UUID.randomUUID().toString(), ".knarrow");
        path.toFile().deleteOnExit();
        return path;
    }

    /**
     * @return a new {@link PythonGateway} for running tests.
     * @throws IOException
     */
    public static PythonGateway<ArrowTestsEntryPoint> openPythonGateway() throws IOException {
        final PythonCommand command = getPythonCommand();
        final String launcherPath =
            Paths.get(System.getProperty("user.dir"), "src/test/python", "tests_launcher.py").toString();
        final PythonPath pythonPath = (new PythonPathBuilder()) //
            .add(removeLeadingSlashOnWindows(PythonModuleKnimeGateway.getPythonModule())) //
            .add(removeLeadingSlashOnWindows(PythonModuleKnimeArrow.getPythonModule())) //
            .build();
        final List<PythonExtension> extensions = Collections.singletonList(PythonArrowExtension.INSTANCE);

        return new PythonGateway<>(command, launcherPath, ArrowTestsEntryPoint.class, extensions, pythonPath);
    }

    /**
     * This function removes the leading '/' of a path extracted from a URL if the operating system is Windows.
     * Paths extracted from a URL typically start with a '/' which on Windows leads to paths like '/C:/...".
     *
     * @param path extracted from a URL
     * @return a path with the leading slash removed
     */
    public static String removeLeadingSlashOnWindows(final String path) {
        if (SystemUtils.IS_OS_WINDOWS && path.startsWith("/")) {
            String withoutLeadingSlash = path.substring(1);
            return withoutLeadingSlash.replace("%20", " ");
        } else {
            return path;
        }
    }

    /**
     * {@link PythonEntryPoint} for the tests. This interface is implemented on Python and calling a method will execute
     * Python code.
     */
    public interface ArrowTestsEntryPoint extends PythonEntryPoint {

        /**
         * Assert that the data is the expected data for the given type.
         *
         * @param dataType the expected type
         * @param dataSource the data
         */
        void testTypeToPython(String dataType, PythonDataSource dataSource);

        /**
         * Create data for the given type and write it to the data sink.
         *
         * @param dataType the type to write
         * @param dataSink the data sink to write to
         */
        void testTypeFromPython(String dataType, PythonDataSink dataSink);

        /**
         * Create data with the schema 0: int, 1: string, 2: struct<list<int>, float> and write it to the sink.
         *
         * @param dataSink the data sink to write to
         */
        void testExpectedSchema(PythonDataSink dataSink);

        /**
         * Assert that the list of data sources is as expected and write to a list of data sinks.
         *
         * @param dataSources sources of data
         * @param dataSinks sinks to write to
         */
        void testMultipleInputsOutputs(List<? extends PythonDataSource> dataSources,
            List<? extends PythonDataSink> dataSinks);
    }
}
