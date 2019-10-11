import os
import unittest
from unittest import mock

from parameterized import parameterized
from importlib.machinery import SourceFileLoader

# TODO: use pathlib
# https://docs.python.org/3/library/pathlib.html


class PlatformParsePortTestCase(unittest.TestCase):
    @parameterized.expand([
        (8888, "# MAGIC\nEXPOSE 8888\nEXPOSE 123 456"),
    ])
    def test_parse_port(self, expected, read_data):
        path = os.path.dirname(os.path.dirname(
            os.path.abspath(__file__))) + '/package'
        m = mock.mock_open(read_data=read_data)
        package = SourceFileLoader('package', path).load_module()
        with mock.patch('package.open', m):
            port = package.parse_expected_http_port(filename='--mocked--')
            self.assertEqual(port, str(expected))
        m.assert_called_once_with('--mocked--')
