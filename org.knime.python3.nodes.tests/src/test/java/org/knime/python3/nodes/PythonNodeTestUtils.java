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
 *   Jan 21, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.python3.nodes;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.knime.python3.DefaultPythonGateway;
import org.knime.python3.Python3SourceDirectory;
import org.knime.python3.Python3TestUtils;
import org.knime.python3.PythonEntryPoint;
import org.knime.python3.PythonExtension;
import org.knime.python3.PythonGateway;
import org.knime.python3.PythonPath;
import org.knime.python3.PythonPath.PythonPathBuilder;
import org.knime.python3.arrow.Python3ArrowSourceDirectory;
import org.knime.python3.arrow.PythonArrowExtension;
import org.knime.python3.types.PythonModule;
import org.knime.python3.views.Python3ViewsSourceDirectory;

/**
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
public final class PythonNodeTestUtils {

    static <E extends PythonEntryPoint> PythonGateway<E> openPythonGateway(final Class<E> entryPointClass,
        final String launcherPath, final PythonModule... modules) throws IOException, InterruptedException {
        final var command = Python3TestUtils.getPythonCommand();
        final PythonPathBuilder builder = PythonPath.builder()//
            .add(Python3SourceDirectory.getPath()) //
            .add(Python3ArrowSourceDirectory.getPath()) //
            .add(Python3ViewsSourceDirectory.getPath());
        for (final PythonModule module : modules) {
            builder.add(module.getParentDirectory());
        }
        final var pythonPath = builder.build();
        final List<PythonExtension> pyExtensions = new ArrayList<>();
        pyExtensions.add(PythonArrowExtension.INSTANCE);

        return DefaultPythonGateway.create(command.createProcessBuilder(), launcherPath, entryPointClass, pyExtensions,
            pythonPath);
    }

    private PythonNodeTestUtils() {

    }
}
