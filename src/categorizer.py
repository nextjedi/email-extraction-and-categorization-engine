"""Email Categorizer Module

Categorizes email addresses based on various criteria such as domain,
keywords, patterns, and custom rules.
"""

from typing import List, Dict, Set, Callable, Optional
from collections import defaultdict
import re


class EmailCategorizer:
    """Categorizes email addresses using various criteria."""

    # Common email domains
    FREE_EMAIL_DOMAINS = {
        'gmail.com', 'yahoo.com', 'hotmail.com', 'outlook.com',
        'aol.com', 'icloud.com', 'mail.com', 'protonmail.com',
        'yandex.com', 'zoho.com'
    }

    BUSINESS_DOMAINS = {
        'company.com', 'corp.com', 'enterprise.com', 'business.com'
    }

    EDUCATIONAL_DOMAINS = {
        'edu', 'ac.uk', 'edu.au', 'edu.cn', 'ac.in'
    }

    def __init__(self):
        """Initialize the EmailCategorizer."""
        self.custom_categories: Dict[str, List[str]] = {}
        self.custom_rules: Dict[str, Callable[[str], bool]] = {}

    def categorize_by_domain(self, emails: List[str]) -> Dict[str, List[str]]:
        """Categorize emails by their domain.

        Args:
            emails: List of email addresses.

        Returns:
            Dictionary mapping domains to lists of email addresses.
        """
        categories = defaultdict(list)

        for email in emails:
            domain = self._extract_domain(email)
            if domain:
                categories[domain].append(email)

        return dict(categories)

    def categorize_by_type(self, emails: List[str]) -> Dict[str, List[str]]:
        """Categorize emails by type (free, business, educational, other).

        Args:
            emails: List of email addresses.

        Returns:
            Dictionary mapping types to lists of email addresses.
        """
        categories = {
            'free': [],
            'business': [],
            'educational': [],
            'government': [],
            'other': []
        }

        for email in emails:
            domain = self._extract_domain(email)
            if not domain:
                categories['other'].append(email)
                continue

            if domain in self.FREE_EMAIL_DOMAINS:
                categories['free'].append(email)
            elif any(domain.endswith(edu_domain) for edu_domain in self.EDUCATIONAL_DOMAINS):
                categories['educational'].append(email)
            elif domain.endswith('.gov') or domain == 'gov.uk' or domain.endswith('.gov.uk'):
                categories['government'].append(email)
            elif domain in self.BUSINESS_DOMAINS or self._is_business_domain(domain):
                categories['business'].append(email)
            else:
                categories['other'].append(email)

        return categories

    def categorize_by_keywords(
        self,
        emails: List[str],
        keywords: Dict[str, List[str]]
    ) -> Dict[str, List[str]]:
        """Categorize emails based on keywords in the email address.

        Args:
            emails: List of email addresses.
            keywords: Dictionary mapping category names to lists of keywords.
                     Example: {'sales': ['sales', 'business'], 'support': ['help', 'support']}

        Returns:
            Dictionary mapping categories to lists of email addresses.
        """
        categories = defaultdict(list)

        for email in emails:
            email_lower = email.lower()
            categorized = False

            for category, keyword_list in keywords.items():
                for keyword in keyword_list:
                    if keyword.lower() in email_lower:
                        categories[category].append(email)
                        categorized = True
                        break
                if categorized:
                    break

            if not categorized:
                categories['uncategorized'].append(email)

        return dict(categories)

    def categorize_by_pattern(
        self,
        emails: List[str],
        patterns: Dict[str, str]
    ) -> Dict[str, List[str]]:
        """Categorize emails based on regex patterns.

        Args:
            emails: List of email addresses.
            patterns: Dictionary mapping category names to regex patterns.
                     Example: {'numeric': r'.*\d+.*@.*', 'admin': r'admin.*@.*'}

        Returns:
            Dictionary mapping categories to lists of email addresses.
        """
        categories = defaultdict(list)
        compiled_patterns = {
            category: re.compile(pattern)
            for category, pattern in patterns.items()
        }

        for email in emails:
            categorized = False

            for category, pattern in compiled_patterns.items():
                if pattern.match(email):
                    categories[category].append(email)
                    categorized = True
                    break

            if not categorized:
                categories['uncategorized'].append(email)

        return dict(categories)

    def categorize_by_custom_rule(
        self,
        emails: List[str],
        rules: Dict[str, Callable[[str], bool]]
    ) -> Dict[str, List[str]]:
        """Categorize emails using custom functions.

        Args:
            emails: List of email addresses.
            rules: Dictionary mapping category names to functions that return True
                  if an email belongs to that category.

        Returns:
            Dictionary mapping categories to lists of email addresses.
        """
        categories = defaultdict(list)

        for email in emails:
            categorized = False

            for category, rule_func in rules.items():
                try:
                    if rule_func(email):
                        categories[category].append(email)
                        categorized = True
                        break
                except Exception as e:
                    print(f"Error in custom rule '{category}' for email '{email}': {str(e)}")
                    continue

            if not categorized:
                categories['uncategorized'].append(email)

        return dict(categories)

    def add_custom_category(self, category_name: str, domains: List[str]):
        """Add a custom category with specific domains.

        Args:
            category_name: Name of the category.
            domains: List of domains belonging to this category.
        """
        self.custom_categories[category_name] = domains

    def add_custom_rule(self, rule_name: str, rule_func: Callable[[str], bool]):
        """Add a custom categorization rule.

        Args:
            rule_name: Name of the rule/category.
            rule_func: Function that takes an email and returns True if it belongs to the category.
        """
        self.custom_rules[rule_name] = rule_func

    def categorize_multi(
        self,
        emails: List[str],
        methods: List[str]
    ) -> Dict[str, Dict[str, List[str]]]:
        """Apply multiple categorization methods.

        Args:
            emails: List of email addresses.
            methods: List of method names to apply ('domain', 'type', etc.).

        Returns:
            Dictionary mapping method names to their categorization results.
        """
        results = {}

        for method in methods:
            if method == 'domain':
                results['domain'] = self.categorize_by_domain(emails)
            elif method == 'type':
                results['type'] = self.categorize_by_type(emails)

        return results

    def get_statistics(self, categorized: Dict[str, List[str]]) -> Dict[str, int]:
        """Get statistics about categorized emails.

        Args:
            categorized: Dictionary of categorized emails.

        Returns:
            Dictionary mapping categories to count of emails.
        """
        return {category: len(emails) for category, emails in categorized.items()}

    @staticmethod
    def _extract_domain(email: str) -> Optional[str]:
        """Extract domain from an email address.

        Args:
            email: Email address.

        Returns:
            Domain name or None if invalid.
        """
        try:
            return email.split('@')[1].lower()
        except (IndexError, AttributeError):
            return None

    @staticmethod
    def _is_business_domain(domain: str) -> bool:
        """Check if a domain appears to be a business domain.

        Args:
            domain: Domain name.

        Returns:
            True if it appears to be a business domain.
        """
        # Heuristic: business domains typically don't end with common free email TLDs
        # But exclude very short domains which are likely services
        parts = domain.split('.')
        return len(parts) >= 2 and domain not in EmailCategorizer.FREE_EMAIL_DOMAINS

    def filter_by_category(
        self,
        categorized: Dict[str, List[str]],
        categories: List[str]
    ) -> List[str]:
        """Filter emails to only include specific categories.

        Args:
            categorized: Dictionary of categorized emails.
            categories: List of category names to include.

        Returns:
            List of emails from the specified categories.
        """
        filtered = []
        for category in categories:
            if category in categorized:
                filtered.extend(categorized[category])
        return filtered

    def exclude_categories(
        self,
        categorized: Dict[str, List[str]],
        categories: List[str]
    ) -> List[str]:
        """Exclude emails from specific categories.

        Args:
            categorized: Dictionary of categorized emails.
            categories: List of category names to exclude.

        Returns:
            List of emails not in the excluded categories.
        """
        filtered = []
        for category, emails in categorized.items():
            if category not in categories:
                filtered.extend(emails)
        return filtered
