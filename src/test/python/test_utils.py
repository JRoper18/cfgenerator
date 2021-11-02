import unittest
from test import support

from src.main.python.utils import remove_attrs, remove_line_attrs

class TestUtils(unittest.TestCase):

    def test_attr_regex_line(self):
        line = "34 {retType=int}"
        removed = remove_line_attrs(line, "retType")
        self.assertEqual(line, removed)
        removed2 = remove_line_attrs(line, "notRetType")
        self.assertEqual("34 {}", removed2)

    def test_attr_regex(self):
        txt = "34 {retType=int}\n\t33 {retType=bool,badType=false}"
        self.assertEqual("34 {retType=int}\n\t33 {retType=bool}", remove_attrs(txt, "retType"))
        self.assertEqual("34 {}\n\t33 {badType=false}", remove_attrs(txt, "badType"))
        self.assertEqual(txt, remove_attrs(txt, "(badType)|(retType)"))
        self.assertEqual("34 {}\n\t33 {}", remove_attrs(txt, "notAKey"))