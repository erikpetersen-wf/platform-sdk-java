import pathlib
import unittest
from unittest import mock

from parameterized import parameterized
from importlib.machinery import SourceFileLoader


class PlatformParsePortTestCase(unittest.TestCase):
    @parameterized.expand([
        (8888, "# MAGIC\nEXPOSE 8888\nEXPOSE 123 456"),
    ])
    def test_parse_port(self, expected, read_data):
        path = pathlib.Path(__file__).parent.with_name('package').as_posix()
        m = mock.mock_open(read_data=read_data)
        package = SourceFileLoader('package', path).load_module()
        with mock.patch('package.open', m):
            port = package.parse_expected_http_port(filename='--mocked--')
            self.assertEqual(port, str(expected))
        m.assert_called_once_with('--mocked--')
