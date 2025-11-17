"""Email Extractor Module

Extracts email addresses from various sources using regex patterns.
"""

import re
from typing import List, Set, Union, Optional
from pathlib import Path


class EmailExtractor:
    """Extracts email addresses from text, files, and other sources."""

    # Comprehensive email regex pattern (RFC 5322 compliant)
    EMAIL_PATTERN = re.compile(
        r'\b[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Z|a-z]{2,}\b'
    )

    def __init__(self, case_sensitive: bool = False):
        """Initialize the EmailExtractor.

        Args:
            case_sensitive: If True, preserve email case. If False, convert to lowercase.
        """
        self.case_sensitive = case_sensitive
        self._extracted_emails: Set[str] = set()

    def extract_from_text(self, text: str) -> List[str]:
        """Extract email addresses from a text string.

        Args:
            text: The text to extract emails from.

        Returns:
            List of unique email addresses found in the text.
        """
        if not text:
            return []

        emails = self.EMAIL_PATTERN.findall(text)

        if not self.case_sensitive:
            emails = [email.lower() for email in emails]

        # Remove duplicates while preserving order
        seen = set()
        unique_emails = []
        for email in emails:
            if email not in seen:
                seen.add(email)
                unique_emails.append(email)
                self._extracted_emails.add(email)

        return unique_emails

    def extract_from_file(self, file_path: Union[str, Path]) -> List[str]:
        """Extract email addresses from a file.

        Args:
            file_path: Path to the file to extract emails from.

        Returns:
            List of unique email addresses found in the file.

        Raises:
            FileNotFoundError: If the file doesn't exist.
            IOError: If there's an error reading the file.
        """
        path = Path(file_path)

        if not path.exists():
            raise FileNotFoundError(f"File not found: {file_path}")

        try:
            with open(path, 'r', encoding='utf-8', errors='ignore') as f:
                text = f.read()
            return self.extract_from_text(text)
        except Exception as e:
            raise IOError(f"Error reading file {file_path}: {str(e)}")

    def extract_from_files(self, file_paths: List[Union[str, Path]]) -> List[str]:
        """Extract email addresses from multiple files.

        Args:
            file_paths: List of file paths to extract emails from.

        Returns:
            List of unique email addresses found across all files.
        """
        all_emails = []
        for file_path in file_paths:
            try:
                emails = self.extract_from_file(file_path)
                all_emails.extend(emails)
            except (FileNotFoundError, IOError) as e:
                print(f"Warning: {str(e)}")
                continue

        # Remove duplicates while preserving order
        seen = set()
        unique_emails = []
        for email in all_emails:
            if email not in seen:
                seen.add(email)
                unique_emails.append(email)

        return unique_emails

    def extract_from_directory(
        self,
        directory_path: Union[str, Path],
        recursive: bool = False,
        file_extensions: Optional[List[str]] = None
    ) -> List[str]:
        """Extract email addresses from all files in a directory.

        Args:
            directory_path: Path to the directory.
            recursive: If True, search subdirectories recursively.
            file_extensions: List of file extensions to process (e.g., ['.txt', '.log']).
                           If None, process all files.

        Returns:
            List of unique email addresses found in the directory.

        Raises:
            NotADirectoryError: If the path is not a directory.
        """
        path = Path(directory_path)

        if not path.is_dir():
            raise NotADirectoryError(f"Not a directory: {directory_path}")

        # Get all files
        if recursive:
            files = path.rglob('*')
        else:
            files = path.glob('*')

        # Filter by extension if specified
        if file_extensions:
            files = [f for f in files if f.is_file() and f.suffix in file_extensions]
        else:
            files = [f for f in files if f.is_file()]

        return self.extract_from_files(files)

    def get_all_extracted(self) -> List[str]:
        """Get all emails extracted during this session.

        Returns:
            List of all unique emails extracted so far.
        """
        return sorted(list(self._extracted_emails))

    def clear_cache(self):
        """Clear the cache of extracted emails."""
        self._extracted_emails.clear()

    def validate_email(self, email: str) -> bool:
        """Validate if a string is a valid email address.

        Args:
            email: The email address to validate.

        Returns:
            True if valid, False otherwise.
        """
        return bool(self.EMAIL_PATTERN.fullmatch(email))

    @staticmethod
    def normalize_email(email: str) -> str:
        """Normalize an email address to lowercase.

        Args:
            email: The email address to normalize.

        Returns:
            Normalized email address.
        """
        return email.lower().strip()
