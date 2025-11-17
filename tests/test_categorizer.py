"""Tests for the EmailCategorizer module."""

import unittest
from pathlib import Path
import sys
sys.path.insert(0, str(Path(__file__).parent.parent))

from src.categorizer import EmailCategorizer


class TestEmailCategorizer(unittest.TestCase):
    """Test cases for EmailCategorizer class."""

    def setUp(self):
        """Set up test fixtures."""
        self.categorizer = EmailCategorizer()
        self.sample_emails = [
            'user@gmail.com',
            'admin@company.com',
            'student@university.edu',
            'support@yahoo.com',
            'info@business.org',
            'contact@gov.uk'
        ]

    def test_categorize_by_domain(self):
        """Test categorization by domain."""
        result = self.categorizer.categorize_by_domain(self.sample_emails)
        self.assertIn('gmail.com', result)
        self.assertIn('company.com', result)
        self.assertEqual(len(result), len(self.sample_emails))

    def test_categorize_by_type(self):
        """Test categorization by type."""
        result = self.categorizer.categorize_by_type(self.sample_emails)
        self.assertIn('free', result)
        self.assertIn('educational', result)
        self.assertIn('government', result)
        # Gmail and Yahoo should be in free
        self.assertIn('user@gmail.com', result['free'])
        self.assertIn('support@yahoo.com', result['free'])
        # .edu should be in educational
        self.assertIn('student@university.edu', result['educational'])
        # .gov should be in government
        self.assertIn('contact@gov.uk', result['government'])

    def test_categorize_by_keywords(self):
        """Test categorization by keywords."""
        emails = [
            'sales@company.com',
            'support@company.com',
            'admin@company.com',
            'random@company.com'
        ]
        keywords = {
            'sales': ['sales'],
            'support': ['support', 'help'],
            'admin': ['admin']
        }
        result = self.categorizer.categorize_by_keywords(emails, keywords)
        self.assertEqual(len(result['sales']), 1)
        self.assertEqual(len(result['support']), 1)
        self.assertEqual(len(result['admin']), 1)
        self.assertEqual(len(result['uncategorized']), 1)

    def test_categorize_by_pattern(self):
        """Test categorization by regex patterns."""
        emails = [
            'user123@example.com',
            'admin@example.com',
            'test@example.com'
        ]
        patterns = {
            'numeric': r'.*\d+.*@.*',
            'admin': r'admin.*@.*'
        }
        result = self.categorizer.categorize_by_pattern(emails, patterns)
        self.assertIn('user123@example.com', result['numeric'])
        self.assertIn('admin@example.com', result['admin'])
        self.assertIn('test@example.com', result['uncategorized'])

    def test_categorize_by_custom_rule(self):
        """Test categorization using custom functions."""
        emails = [
            'short@ex.com',
            'verylongemailaddress@example.com',
            'mid@example.org'
        ]

        def is_long(email):
            return len(email) > 20

        def is_short(email):
            return len(email) < 15

        rules = {
            'long': is_long,
            'short': is_short
        }

        result = self.categorizer.categorize_by_custom_rule(emails, rules)
        self.assertTrue(len(result['long']) > 0)
        self.assertTrue(len(result['short']) > 0)

    def test_get_statistics(self):
        """Test getting statistics from categorized emails."""
        categorized = {
            'category1': ['email1@example.com', 'email2@example.com'],
            'category2': ['email3@example.com']
        }
        stats = self.categorizer.get_statistics(categorized)
        self.assertEqual(stats['category1'], 2)
        self.assertEqual(stats['category2'], 1)

    def test_extract_domain(self):
        """Test domain extraction from email."""
        email = 'user@example.com'
        domain = EmailCategorizer._extract_domain(email)
        self.assertEqual(domain, 'example.com')

    def test_extract_domain_invalid(self):
        """Test domain extraction from invalid email."""
        email = 'notanemail'
        domain = EmailCategorizer._extract_domain(email)
        self.assertIsNone(domain)

    def test_filter_by_category(self):
        """Test filtering emails by category."""
        categorized = {
            'free': ['user@gmail.com', 'test@yahoo.com'],
            'business': ['admin@company.com'],
            'educational': ['student@university.edu']
        }
        result = self.categorizer.filter_by_category(
            categorized,
            ['free', 'educational']
        )
        self.assertEqual(len(result), 3)
        self.assertIn('user@gmail.com', result)
        self.assertIn('student@university.edu', result)
        self.assertNotIn('admin@company.com', result)

    def test_exclude_categories(self):
        """Test excluding specific categories."""
        categorized = {
            'free': ['user@gmail.com'],
            'business': ['admin@company.com'],
            'educational': ['student@university.edu']
        }
        result = self.categorizer.exclude_categories(categorized, ['free'])
        self.assertEqual(len(result), 2)
        self.assertNotIn('user@gmail.com', result)
        self.assertIn('admin@company.com', result)

    def test_add_custom_category(self):
        """Test adding custom categories."""
        self.categorizer.add_custom_category('tech', ['github.com', 'gitlab.com'])
        self.assertIn('tech', self.categorizer.custom_categories)
        self.assertEqual(len(self.categorizer.custom_categories['tech']), 2)

    def test_add_custom_rule(self):
        """Test adding custom rules."""
        def custom_rule(email):
            return 'vip' in email

        self.categorizer.add_custom_rule('vip', custom_rule)
        self.assertIn('vip', self.categorizer.custom_rules)


if __name__ == '__main__':
    unittest.main()
