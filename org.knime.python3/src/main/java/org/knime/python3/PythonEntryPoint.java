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
 *   May 3, 2021 (benjamin): created
 */
package org.knime.python3;

import java.util.List;

/**
 * Base interface for all Python entry points. A Python entry point defines methods that must be implemented by a Python
 * class. When calling these methods Python will be invoked. To use the {@link PythonGateway} all Python entry points
 * must implement the methods defined here.
 *
 * Note that the methods defined here are implemented by <code>knime.client.EntryPoint</code>. When implementing an
 * interface that extends {@link PythonEntryPoint} as a Python class this class should extend
 * <code>knime.client.EntryPoint</code>.
 *
 * @author Benjamin Wilhelm, KNIME GmbH, Konstanz, Germany
 */
public interface PythonEntryPoint {

    /**
     * @return the process identifier (PID) of the Python process
     */
    int getPid();

    /**
     * Equivalent to calling {@code enableDebugging(pydevdModuleDirectoryPath, true, true, false)}.
     */
    void enableDebugging(String pydevdModuleDirectoryPath);

    /**
     * @param pydevdModuleDirectoryPath May be {@code null} if {@code enableBreakpoints} is {@code false}.
     */
    void enableDebugging(String pydevdModuleDirectoryPath, boolean enableBreakpoints, boolean enableDebugLog,
        boolean debugLogToStderr);

    /**
     * Registers a list of extensions to the Python integration. Each extension is imported using
     * <code>importlib.import_module(ext)</code>. During the import extensions can register themselves at appropriate
     * endpoints.
     *
     * @param extensions a list of Python modules that will be imported
     */
    void registerExtensions(List<String> extensions);
}
