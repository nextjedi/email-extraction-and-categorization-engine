"""Tests for the EmailEngine module."""

import unittest
import tempfile
import json
import csv
from pathlib import Path
import sys
sys.path.insert(0, str(Path(__file__).parent.parent))

from src.engine import EmailEngine


class TestEmailEngine(unittest.TestCase):
    """Test cases for EmailEngine class."""

    def setUp(self):
        """Set up test fixtures."""
        self.engine = EmailEngine()
        self.sample_text = """
        Contact our sales team at sales@company.com
        Support: support@company.com
        General inquiries: info@gmail.com
        """

    def tearDown(self):
        """Clean up after tests."""
        self.engine.reset()

    def test_extract_from_text(self):
        """Test extracting emails from text."""
        emails = self.engine.extract(self.sample_text, source_type='text')
        self.assertEqual(len(emails), 3)
        self.assertIn('sales@company.com', emails)
        self.assertIn('support@company.com', emails)
        self.assertIn('info@gmail.com', emails)

    def test_extract_from_file(self):
        """Test extracting emails from a file."""
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            f.write(self.sample_text)
            temp_file = f.name

        try:
            emails = self.engine.extract(temp_file, source_type='file')
            self.assertEqual(len(emails), 3)
        finally:
            Path(temp_file).unlink()

    def test_categorize_by_type(self):
        """Test categorizing emails by type."""
        self.engine.extract(self.sample_text, source_type='text')
        categorized = self.engine.categorize(method='type')
        self.assertIn('free', categorized)
        self.assertIn('other', categorized)

    def test_categorize_by_domain(self):
        """Test categorizing emails by domain."""
        self.engine.extract(self.sample_text, source_type='text')
        categorized = self.engine.categorize(method='domain')
        self.assertIn('company.com', categorized)
        self.assertIn('gmail.com', categorized)

    def test_categorize_without_extraction(self):
        """Test that categorize raises error without prior extraction."""
        with self.assertRaises(ValueError):
            self.engine.categorize()

    def test_process_all_in_one(self):
        """Test the process method that extracts and categorizes."""
        result = self.engine.process(
            self.sample_text,
            source_type='text',
            categorization_method='type'
        )
        self.assertIn('total_emails', result)
        self.assertIn('emails', result)
        self.assertIn('categorized', result)
        self.assertIn('statistics', result)
        self.assertEqual(result['total_emails'], 3)

    def test_export_to_json(self):
        """Test exporting results to JSON."""
        self.engine.extract(self.sample_text, source_type='text')
        self.engine.categorize(method='type')

        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.json') as f:
            temp_file = f.name

        try:
            self.engine.export_to_json(temp_file, include_stats=True)
            with open(temp_file, 'r') as f:
                data = json.load(f)
            self.assertIn('emails', data)
            self.assertIn('categorized', data)
            self.assertIn('statistics', data)
        finally:
            Path(temp_file).unlink()

    def test_export_to_csv(self):
        """Test exporting results to CSV."""
        self.engine.extract(self.sample_text, source_type='text')
        self.engine.categorize(method='type')

        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.csv') as f:
            temp_file = f.name

        try:
            self.engine.export_to_csv(temp_file, include_category=True)
            with open(temp_file, 'r') as f:
                reader = csv.reader(f)
                rows = list(reader)
            self.assertEqual(rows[0], ['Email', 'Category'])
            self.assertGreater(len(rows), 1)
        finally:
            Path(temp_file).unlink()

    def test_export_to_txt(self):
        """Test exporting results to text file."""
        self.engine.extract(self.sample_text, source_type='text')
        self.engine.categorize(method='type')

        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            temp_file = f.name

        try:
            self.engine.export_to_txt(temp_file, grouped_by_category=True)
            with open(temp_file, 'r') as f:
                content = f.read()
            self.assertTrue(len(content) > 0)
        finally:
            Path(temp_file).unlink()

    def test_get_summary(self):
        """Test getting summary of extraction and categorization."""
        self.engine.extract(self.sample_text, source_type='text')
        self.engine.categorize(method='type')
        summary = self.engine.get_summary()
        self.assertIn('total_emails', summary)
        self.assertIn('has_categorization', summary)
        self.assertTrue(summary['has_categorization'])
        self.assertEqual(summary['total_emails'], 3)

    def test_filter_emails_by_category(self):
        """Test filtering emails by category."""
        self.engine.extract(self.sample_text, source_type='text')
        self.engine.categorize(method='type')
        filtered = self.engine.filter_emails(categories=['free'])
        self.assertTrue(len(filtered) > 0)

    def test_filter_emails_without_categorization(self):
        """Test that filter raises error without categorization."""
        self.engine.extract(self.sample_text, source_type='text')
        with self.assertRaises(ValueError):
            self.engine.filter_emails(categories=['free'])

    def test_reset(self):
        """Test resetting the engine."""
        self.engine.extract(self.sample_text, source_type='text')
        self.engine.categorize(method='type')
        self.engine.reset()
        self.assertEqual(len(self.engine.emails), 0)
        self.assertEqual(len(self.engine.categorized), 0)

    def test_repr(self):
        """Test string representation."""
        repr_str = repr(self.engine)
        self.assertIn('EmailEngine', repr_str)
        self.assertIn('emails=', repr_str)


if __name__ == '__main__':
    unittest.main()
