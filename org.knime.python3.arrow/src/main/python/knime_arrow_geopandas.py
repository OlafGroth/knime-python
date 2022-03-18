# -*- coding: utf-8 -*-
# ------------------------------------------------------------------------
#  Copyright by KNIME AG, Zurich, Switzerland
#  Website: http://www.knime.com; Email: contact@knime.com
#
#  This program is free software; you can redistribute it and/or modify
#  it under the terms of the GNU General Public License, Version 3, as
#  published by the Free Software Foundation.
#
#  This program is distributed in the hope that it will be useful, but
#  WITHOUT ANY WARRANTY; without even the implied warranty of
#  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
#  GNU General Public License for more details.
#
#  You should have received a copy of the GNU General Public License
#  along with this program; if not, see <http://www.gnu.org/licenses>.
#
#  Additional permission under GNU GPL version 3 section 7:
#
#  KNIME interoperates with ECLIPSE solely via ECLIPSE's plug-in APIs.
#  Hence, KNIME and ECLIPSE are both independent programs and are not
#  derived from each other. Should, however, the interpretation of the
#  GNU GPL Version 3 ("License") under any applicable laws result in
#  KNIME and ECLIPSE being a combined program, KNIME AG herewith grants
#  you the additional permission to use and propagate KNIME together with
#  ECLIPSE with only the license terms in place for ECLIPSE applying to
#  ECLIPSE and the GNU GPL Version 3 applying for KNIME, provided the
#  license terms of ECLIPSE themselves allow for the respective use and
#  propagation of ECLIPSE together with KNIME.
#
#  Additional permission relating to nodes for KNIME that extend the Node
#  Extension (and in particular that are based on subclasses of NodeModel,
#  NodeDialog, and NodeView) and that only interoperate with KNIME through
#  standard APIs ("Nodes"):
#  Nodes are deemed to be separate and independent programs and to not be
#  covered works.  Notwithstanding anything to the contrary in the
#  License, the License does not apply to Nodes, you are not required to
#  license Nodes under the License, and you are granted a license to
#  prepare and propagate Nodes, in each case even if such Nodes are
#  propagated with or for interoperation with KNIME.  The owner of a Node
#  may freely choose the license terms applicable to such Node, including
#  when such Node is propagated with or for interoperation with KNIME.
# ------------------------------------------------------------------------

from typing import Type, Union

from pandas.core.dtypes.dtypes import register_extension_dtype
import pandas.api.extensions as pdext

import pyarrow as pa
import knime_gateway as kg
import knime_types as kt
import knime_arrow_types as kat
import knime_arrow_struct_dict_encoding as kas
import pandas as pd
import numpy as np
import geopandas
import knime_arrow_pandas as kap


@register_extension_dtype
class PandasLogicalGeoTypeExtensionType(
    geopandas.array.GeometryDtype, kap.PandasLogicalTypeExtensionType
):
    def __init__(self, storage_type: pa.DataType, logical_type: str, converter):
        super().__init__(storage_type, logical_type, converter)

    @property
    def name(self):
        return f"PandasLogicalGeoTypeExtensionType({self._storage_type}, {self._logical_type})"

    def construct_array_type(self):
        return KnimeGeoPandasExtensionArray

    def __from_arrow__(self, arrow_array):
        return KnimeGeoPandasExtensionArray(
            self._storage_type, self._logical_type, self._converter, arrow_array
        )

    def __str__(self):
        return f"PandasLogicalGeoTypeExtensionType({self._storage_type}, {self._logical_type})"


class KnimeGeoPandasExtensionArray(
    geopandas.array.GeometryArray, kap.KnimePandasExensionArray
):
    def __init__(
        self,
        storage_type: pa.DataType,
        logical_type: str,
        converter,
        data: Union[pa.Array, pa.ChunkedArray],
    ):
        super().__init__(storage_type, logical_type, converter, data)

    def __arrow_array__(self, type=None):
        return self._data

    @classmethod
    def _from_sequence(
        cls,
        scalars,
        dtype=None,
        copy=None,
        storage_type=None,
        logical_type=None,
        converter=None,
    ):
        if scalars is None:
            raise ValueError(
                "Cannot create KnimeGeoPandasExtensionArray from empty data"
            )

        # easy case
        if isinstance(scalars, pa.Array) or isinstance(scalars, pa.ChunkedArray):
            if (
                not isinstance(scalars.type, kat.LogicalTypeExtensionType)
                or not "Geo" in scalars.type.logical_type
            ):
                raise ValueError(
                    "KnimeGeoPandasExtensionArray must be backed by LogicalTypeExtensionType values with logical type Geo"
                )
            return KnimeGeoPandasExtensionArray(
                scalars.type.storage_type,
                scalars.type.logical_type,
                scalars.type._converter,
                scalars,
            )

        if isinstance(dtype, PandasLogicalGeoTypeExtensionType):
            # in this case we can extract storage, logical_type and converter
            storage_type = dtype._storage_type
            logical_type = dtype._logical_type
            converter = dtype._converter
            if converter is not None and converter.needs_conversion():
                scalars = [converter.encode(s) for s in scalars]

        if storage_type is None:
            raise ValueError(
                "Can only create KnimeGeoPandasExtensionArray from a sequence if the storage type is given."
            )

        # needed for pandas ExtensionArray API
        arrow_type = kat.LogicalTypeExtensionType(converter, storage_type, logical_type)

        a = pa.array(scalars, type=storage_type)
        extension_array = pa.ExtensionArray.from_storage(arrow_type, a)
        return KnimeGeoPandasExtensionArray(
            storage_type, logical_type, converter, extension_array
        )

    def __eq__(self, other) -> bool:
        if not isinstance(other, KnimeGeoPandasExtensionArray):
            return False
        return (
            other._storage_type == self._storage_type
            and other._logical_type == self._logical_type
            and other._converter == self._converter
            and other._data == self._data
        )

    @property
    def dtype(self):
        # needed for pandas ExtensionArray API
        return PandasLogicalGeoTypeExtensionType(
            self._storage_type, self._logical_type, self._converter
        )

    @classmethod
    def _concat_same_type(cls, to_concat):
        # TODO use super class method to concatenate and only adjust type

        # needed for pandas ExtensionArray API

        if len(to_concat) < 1:
            raise ValueError("Nothing to concatenate")
        elif len(to_concat) == 1:
            return to_concat[0]

        chunks = []
        for pandas_ext_array in to_concat:
            d = pandas_ext_array._data
            if isinstance(d, pa.ChunkedArray):
                chunks += d.chunks
            else:
                chunks.append(d)

        combined_data = pa.chunked_array(chunks)
        first = to_concat[0]
        return KnimeGeoPandasExtensionArray(
            first._storage_type, first._logical_type, first._converter, combined_data
        )
