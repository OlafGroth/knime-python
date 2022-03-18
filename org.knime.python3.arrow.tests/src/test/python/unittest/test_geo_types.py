from os import pardir
from typing import Type, Union
import unittest
import pandas as pd
import pandas.api.extensions as pdext
from pandas.core.dtypes.dtypes import register_extension_dtype
import pyarrow as pa
import numpy as np

import pythonpath
import knime_arrow_pandas as kap
import knime_arrow_types as katy
import knime_arrow as knar


class TestDataSource:
    def __init__(self, absolute_path):
        self.absolute_path = absolute_path

    def getAbsolutePath(self):
        return self.absolute_path

    def isFooterWritten(self):
        return True

    def hasColumnNames(self):
        return False


class PyArrowExtensionTypeTest(unittest.TestCase):
    def _generate_test_table(self):
        # returns a table with: RowKey, WKT (string) and GeoPoint columns
        knime_generated_table_path = "geospatial_table_2.zip"

        test_data_source = TestDataSource(knime_generated_table_path)
        pa_data_source = knar.ArrowDataSource(test_data_source)
        arrow = pa_data_source.to_arrow_table()
        arrow = katy.unwrap_primitive_arrays(arrow)

        return arrow

    def _to_pandas(self, arrow):
        return kap.arrow_data_to_pandas_df(arrow)

    def test_load_table(self):
        t = self._generate_test_table()
        print(t[2][0].as_py())

    def test_load_table(self):
        t = self._generate_test_table()
        df = self._to_pandas(t)
        print(df["geometry"][0])


if __name__ == "__main__":
    unittest.main()
