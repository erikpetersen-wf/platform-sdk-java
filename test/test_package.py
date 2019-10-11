import unittest
from unittest import mock

from parameterized import parameterized

# # monkey patch import for dynamic
import os
# import sys
# import importlib as imp
# sys.path.append(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
# print(os.path.dirname(os.path.dirname(os.path.abspath(__file__))))
# import .. as platform  # noqa

# TODO: use pathlib
# https://docs.python.org/3/library/pathlib.html

# import imp
# print(path)
# package = imp.load_source('package', path)

from importlib.machinery import SourceFileLoader


class PlatformParsePortTestCase(unittest.TestCase):
    @parameterized.expand([
        (8888, "# MAGIC\nEXPOSE 8888\nEXPOSE 123 456"),
    ])
    def test_parse_port(self, expected, read_data):
        path = os.path.dirname(os.path.dirname(
            os.path.abspath(__file__))) + '/package'

        # print(dir(package))
        # print(package.__file__)
        m = mock.mock_open(read_data=read_data)
        with mock.patch('__main__.open', m):
            package = SourceFileLoader('package', path).load_module()
            port = package.parse_expected_http_port(filename='--mocked--')
            self.assertEqual(port, expected)
        m.assert_called_once_with('--mocked--')
