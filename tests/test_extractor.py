"""Tests for the EmailExtractor module."""

import unittest
import tempfile
from pathlib import Path

import sys
sys.path.insert(0, str(Path(__file__).parent.parent))

from src.extractor import EmailExtractor


class TestEmailExtractor(unittest.TestCase):
    """Test cases for EmailExtractor class."""

    def setUp(self):
        """Set up test fixtures."""
        self.extractor = EmailExtractor()

    def tearDown(self):
        """Clean up after tests."""
        self.extractor.clear_cache()

    def test_extract_from_simple_text(self):
        """Test extracting emails from simple text."""
        text = "Contact us at support@example.com or sales@example.org"
        emails = self.extractor.extract_from_text(text)
        self.assertEqual(len(emails), 2)
        self.assertIn('support@example.com', emails)
        self.assertIn('sales@example.org', emails)

    def test_extract_from_empty_text(self):
        """Test extracting from empty text."""
        emails = self.extractor.extract_from_text("")
        self.assertEqual(emails, [])

    def test_extract_with_duplicates(self):
        """Test that duplicates are removed."""
        text = "Email: test@example.com and test@example.com again"
        emails = self.extractor.extract_from_text(text)
        self.assertEqual(len(emails), 1)
        self.assertEqual(emails[0], 'test@example.com')

    def test_case_sensitive_false(self):
        """Test case insensitive extraction (default)."""
        text = "Contact Test@Example.COM or test@example.com"
        emails = self.extractor.extract_from_text(text)
        self.assertEqual(len(emails), 1)
        self.assertEqual(emails[0], 'test@example.com')

    def test_case_sensitive_true(self):
        """Test case sensitive extraction."""
        extractor = EmailExtractor(case_sensitive=True)
        text = "Contact Test@Example.COM or test@example.com"
        emails = extractor.extract_from_text(text)
        self.assertEqual(len(emails), 2)

    def test_extract_complex_emails(self):
        """Test extracting complex email formats."""
        text = """
        john.doe+filter@example.com
        user_name@sub.domain.example.org
        admin-123@company.co.uk
        """
        emails = self.extractor.extract_from_text(text)
        self.assertEqual(len(emails), 3)

    def test_extract_from_file(self):
        """Test extracting emails from a file."""
        with tempfile.NamedTemporaryFile(mode='w', delete=False, suffix='.txt') as f:
            f.write("Email1: test1@example.com\n")
            f.write("Email2: test2@example.org\n")
            temp_file = f.name

        try:
            emails = self.extractor.extract_from_file(temp_file)
            self.assertEqual(len(emails), 2)
            self.assertIn('test1@example.com', emails)
            self.assertIn('test2@example.org', emails)
        finally:
            Path(temp_file).unlink()

    def test_extract_from_nonexistent_file(self):
        """Test extracting from a file that doesn't exist."""
        with self.assertRaises(FileNotFoundError):
            self.extractor.extract_from_file('/nonexistent/path/file.txt')

    def test_validate_email_valid(self):
        """Test email validation with valid emails."""
        valid_emails = [
            'test@example.com',
            'user.name@example.org',
            'admin+tag@company.co.uk'
        ]
        for email in valid_emails:
            self.assertTrue(self.extractor.validate_email(email))

    def test_validate_email_invalid(self):
        """Test email validation with invalid emails."""
        invalid_emails = [
            'notanemail',
            '@example.com',
            'test@',
            'test @example.com',
            ''
        ]
        for email in invalid_emails:
            self.assertFalse(self.extractor.validate_email(email))

    def test_normalize_email(self):
        """Test email normalization."""
        email = "  Test@Example.COM  "
        normalized = EmailExtractor.normalize_email(email)
        self.assertEqual(normalized, 'test@example.com')

    def test_get_all_extracted(self):
        """Test getting all extracted emails from cache."""
        self.extractor.extract_from_text("email1@example.com")
        self.extractor.extract_from_text("email2@example.org")
        all_emails = self.extractor.get_all_extracted()
        self.assertEqual(len(all_emails), 2)

    def test_clear_cache(self):
        """Test clearing the extraction cache."""
        self.extractor.extract_from_text("test@example.com")
        self.extractor.clear_cache()
        all_emails = self.extractor.get_all_extracted()
        self.assertEqual(len(all_emails), 0)


if __name__ == '__main__':
    unittest.main()
