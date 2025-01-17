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
 *   Jun 5, 2022 (Adrian Nembach, KNIME GmbH, Konstanz, Germany): created
 */
package org.knime.python3.nodes;

import org.knime.core.node.NodeLogger;
import org.knime.python3.PythonGateway;
import org.knime.python3.PythonGatewayUtils;
import org.knime.python3.nodes.PurePythonNodeSetFactory.ResolvedPythonExtension;
import org.knime.python3.utils.AutoCloser;

/**
 * Creates CloseablePythonNodeProxy objects for NodeProxyProvider implementations.
 *
 * @author Adrian Nembach, KNIME GmbH, Konstanz, Germany
 */
final class CloseablePythonNodeProxyFactory {

    private static final NodeLogger LOGGER = NodeLogger.getLogger(CloseablePythonNodeProxyFactory.class);

    private final ResolvedPythonExtension m_extension;

    private final String m_nodeId;

    CloseablePythonNodeProxyFactory(final ResolvedPythonExtension extension, final String nodeId) {
        m_extension = extension;
        m_nodeId = nodeId;
    }

    @SuppressWarnings("resource") // the closer is closed when the returned object is closed
    CloseablePythonNodeProxy createProxy(final PythonGateway<KnimeNodeBackend> gateway) {
        final var backend = gateway.getEntryPoint();
        var outputRetrieverHandle = PythonGatewayUtils.redirectGatewayOutput(gateway, LOGGER::info, LOGGER::debug, 100);
        final var callback = new KnimeNodeBackend.Callback() {
            private LogCallback m_logCallback = new DefaultLogCallback(LOGGER);

            @Override
            public void log(final String message, final String severity) {
                m_logCallback.log(message, severity);
            }
        };
        backend.initializeJavaCallback(callback);
        var nodeProxy = m_extension.createProxy(backend, m_nodeId);
        var closer = new AutoCloser(gateway, outputRetrieverHandle);
        return new CloseablePythonNodeProxy(nodeProxy, closer, m_extension.getNode(m_nodeId));
    }

}
