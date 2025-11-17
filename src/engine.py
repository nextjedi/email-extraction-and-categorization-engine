"""Email Engine Module

Main engine that integrates email extraction and categorization functionality.
Provides a high-level interface for processing emails.
"""

from typing import List, Dict, Union, Optional, Any
from pathlib import Path
import json
import csv

from .extractor import EmailExtractor
from .categorizer import EmailCategorizer


class EmailEngine:
    """Main engine for email extraction and categorization."""

    def __init__(self, case_sensitive: bool = False):
        """Initialize the EmailEngine.

        Args:
            case_sensitive: If True, preserve email case. If False, convert to lowercase.
        """
        self.extractor = EmailExtractor(case_sensitive=case_sensitive)
        self.categorizer = EmailCategorizer()
        self.emails: List[str] = []
        self.categorized: Dict[str, List[str]] = {}

    def extract(
        self,
        source: Union[str, Path, List[Union[str, Path]]],
        source_type: str = 'text'
    ) -> List[str]:
        """Extract emails from various sources.

        Args:
            source: The source to extract from (text, file path, or list of file paths).
            source_type: Type of source ('text', 'file', 'files', 'directory').

        Returns:
            List of extracted email addresses.

        Raises:
            ValueError: If source_type is invalid.
        """
        if source_type == 'text':
            self.emails = self.extractor.extract_from_text(str(source))
        elif source_type == 'file':
            self.emails = self.extractor.extract_from_file(source)
        elif source_type == 'files':
            if not isinstance(source, list):
                raise ValueError("For 'files' source_type, source must be a list")
            self.emails = self.extractor.extract_from_files(source)
        elif source_type == 'directory':
            self.emails = self.extractor.extract_from_directory(source)
        else:
            raise ValueError(
                f"Invalid source_type: {source_type}. "
                f"Must be one of: 'text', 'file', 'files', 'directory'"
            )

        return self.emails

    def categorize(
        self,
        method: str = 'type',
        **kwargs
    ) -> Dict[str, List[str]]:
        """Categorize the extracted emails.

        Args:
            method: Categorization method ('domain', 'type', 'keywords', 'pattern', 'custom').
            **kwargs: Additional arguments for specific categorization methods.
                     - keywords: Dict[str, List[str]] for 'keywords' method
                     - patterns: Dict[str, str] for 'pattern' method
                     - rules: Dict[str, Callable] for 'custom' method

        Returns:
            Dictionary mapping categories to lists of emails.

        Raises:
            ValueError: If method is invalid or required kwargs are missing.
        """
        if not self.emails:
            raise ValueError("No emails to categorize. Run extract() first.")

        if method == 'domain':
            self.categorized = self.categorizer.categorize_by_domain(self.emails)
        elif method == 'type':
            self.categorized = self.categorizer.categorize_by_type(self.emails)
        elif method == 'keywords':
            keywords = kwargs.get('keywords')
            if not keywords:
                raise ValueError("'keywords' argument required for keywords method")
            self.categorized = self.categorizer.categorize_by_keywords(
                self.emails, keywords
            )
        elif method == 'pattern':
            patterns = kwargs.get('patterns')
            if not patterns:
                raise ValueError("'patterns' argument required for pattern method")
            self.categorized = self.categorizer.categorize_by_pattern(
                self.emails, patterns
            )
        elif method == 'custom':
            rules = kwargs.get('rules')
            if not rules:
                raise ValueError("'rules' argument required for custom method")
            self.categorized = self.categorizer.categorize_by_custom_rule(
                self.emails, rules
            )
        else:
            raise ValueError(
                f"Invalid method: {method}. "
                f"Must be one of: 'domain', 'type', 'keywords', 'pattern', 'custom'"
            )

        return self.categorized

    def process(
        self,
        source: Union[str, Path, List[Union[str, Path]]],
        source_type: str = 'text',
        categorization_method: str = 'type',
        **kwargs
    ) -> Dict[str, Any]:
        """Extract and categorize emails in one step.

        Args:
            source: The source to extract from.
            source_type: Type of source ('text', 'file', 'files', 'directory').
            categorization_method: Categorization method to use.
            **kwargs: Additional arguments for categorization.

        Returns:
            Dictionary containing extracted emails, categorized emails, and statistics.
        """
        # Extract
        emails = self.extract(source, source_type)

        # Categorize
        categorized = self.categorize(categorization_method, **kwargs)

        # Get statistics
        stats = self.categorizer.get_statistics(categorized)

        return {
            'total_emails': len(emails),
            'emails': emails,
            'categorized': categorized,
            'statistics': stats
        }

    def export_to_json(
        self,
        output_path: Union[str, Path],
        include_stats: bool = True
    ) -> None:
        """Export results to JSON file.

        Args:
            output_path: Path to the output JSON file.
            include_stats: If True, include statistics in the output.
        """
        data = {
            'emails': self.emails,
            'categorized': self.categorized
        }

        if include_stats and self.categorized:
            data['statistics'] = self.categorizer.get_statistics(self.categorized)

        with open(output_path, 'w', encoding='utf-8') as f:
            json.dump(data, f, indent=2, ensure_ascii=False)

    def export_to_csv(
        self,
        output_path: Union[str, Path],
        include_category: bool = True
    ) -> None:
        """Export results to CSV file.

        Args:
            output_path: Path to the output CSV file.
            include_category: If True, include category column.
        """
        with open(output_path, 'w', newline='', encoding='utf-8') as f:
            if include_category and self.categorized:
                writer = csv.writer(f)
                writer.writerow(['Email', 'Category'])

                for category, emails in self.categorized.items():
                    for email in emails:
                        writer.writerow([email, category])
            else:
                writer = csv.writer(f)
                writer.writerow(['Email'])
                for email in self.emails:
                    writer.writerow([email])

    def export_to_txt(
        self,
        output_path: Union[str, Path],
        grouped_by_category: bool = False
    ) -> None:
        """Export results to text file.

        Args:
            output_path: Path to the output text file.
            grouped_by_category: If True, group emails by category.
        """
        with open(output_path, 'w', encoding='utf-8') as f:
            if grouped_by_category and self.categorized:
                for category, emails in self.categorized.items():
                    f.write(f"\n=== {category.upper()} ===\n")
                    for email in emails:
                        f.write(f"{email}\n")
            else:
                for email in self.emails:
                    f.write(f"{email}\n")

    def get_summary(self) -> Dict[str, Any]:
        """Get a summary of the current extraction and categorization.

        Returns:
            Dictionary containing summary information.
        """
        summary = {
            'total_emails': len(self.emails),
            'has_categorization': bool(self.categorized)
        }

        if self.categorized:
            summary['categories'] = list(self.categorized.keys())
            summary['statistics'] = self.categorizer.get_statistics(self.categorized)

        return summary

    def filter_emails(
        self,
        categories: Optional[List[str]] = None,
        exclude_categories: Optional[List[str]] = None
    ) -> List[str]:
        """Filter emails by category.

        Args:
            categories: List of categories to include (None = all).
            exclude_categories: List of categories to exclude.

        Returns:
            Filtered list of emails.
        """
        if not self.categorized:
            raise ValueError("No categorization available. Run categorize() first.")

        if categories:
            return self.categorizer.filter_by_category(self.categorized, categories)
        elif exclude_categories:
            return self.categorizer.exclude_categories(
                self.categorized, exclude_categories
            )
        else:
            return self.emails

    def reset(self):
        """Reset the engine, clearing all extracted and categorized emails."""
        self.emails = []
        self.categorized = {}
        self.extractor.clear_cache()

    def __repr__(self) -> str:
        """String representation of the engine."""
        return (
            f"EmailEngine(emails={len(self.emails)}, "
            f"categorized={bool(self.categorized)})"
        )
