#!/usr/bin/env python3
"""Demo script showing various uses of the Email Extraction and Categorization Engine."""

import sys
from pathlib import Path

# Add parent directory to path to import src
sys.path.insert(0, str(Path(__file__).parent.parent))

from src.engine import EmailEngine
from src.extractor import EmailExtractor
from src.categorizer import EmailCategorizer


def demo_basic_extraction():
    """Demonstrate basic email extraction."""
    print("\n" + "="*60)
    print("DEMO 1: Basic Email Extraction")
    print("="*60)

    text = """
    Contact our team:
    - Sales: sales@company.com
    - Support: support@company.com
    - Info: info@gmail.com
    """

    extractor = EmailExtractor()
    emails = extractor.extract_from_text(text)

    print(f"\nFound {len(emails)} emails:")
    for email in emails:
        print(f"  - {email}")


def demo_file_extraction():
    """Demonstrate extracting from a file."""
    print("\n" + "="*60)
    print("DEMO 2: Extract from File")
    print("="*60)

    sample_file = Path(__file__).parent / "sample_data.txt"

    engine = EmailEngine()
    emails = engine.extract(sample_file, source_type='file')

    print(f"\nExtracted {len(emails)} emails from sample_data.txt")
    print(f"First 5 emails: {emails[:5]}")


def demo_categorization_by_type():
    """Demonstrate categorization by type."""
    print("\n" + "="*60)
    print("DEMO 3: Categorize by Type")
    print("="*60)

    sample_file = Path(__file__).parent / "sample_data.txt"

    engine = EmailEngine()
    result = engine.process(
        sample_file,
        source_type='file',
        categorization_method='type'
    )

    print(f"\nTotal emails found: {result['total_emails']}")
    print("\nBreakdown by type:")
    for category, count in result['statistics'].items():
        print(f"  {category}: {count}")

    print("\nFree email providers:")
    for email in result['categorized'].get('free', [])[:5]:
        print(f"  - {email}")


def demo_categorization_by_domain():
    """Demonstrate categorization by domain."""
    print("\n" + "="*60)
    print("DEMO 4: Categorize by Domain")
    print("="*60)

    sample_file = Path(__file__).parent / "sample_data.txt"

    engine = EmailEngine()
    engine.extract(sample_file, source_type='file')
    categorized = engine.categorize(method='domain')

    print(f"\nFound {len(categorized)} different domains:")
    for domain, emails in list(categorized.items())[:5]:
        print(f"\n  {domain}: ({len(emails)} emails)")
        for email in emails[:3]:
            print(f"    - {email}")


def demo_keyword_categorization():
    """Demonstrate keyword-based categorization."""
    print("\n" + "="*60)
    print("DEMO 5: Categorize by Keywords")
    print("="*60)

    sample_file = Path(__file__).parent / "sample_data.txt"

    engine = EmailEngine()
    engine.extract(sample_file, source_type='file')

    keywords = {
        'sales': ['sales', 'enterprise'],
        'support': ['support', 'help', 'tech'],
        'info': ['info', 'contact', 'general'],
        'noreply': ['noreply', 'donotreply', 'automated']
    }

    categorized = engine.categorize(method='keywords', keywords=keywords)

    print("\nCategorized by keywords:")
    for category, emails in categorized.items():
        print(f"\n  {category}: ({len(emails)} emails)")
        for email in emails[:3]:
            print(f"    - {email}")


def demo_custom_rules():
    """Demonstrate custom categorization rules."""
    print("\n" + "="*60)
    print("DEMO 6: Custom Categorization Rules")
    print("="*60)

    sample_file = Path(__file__).parent / "sample_data.txt"

    engine = EmailEngine()
    engine.extract(sample_file, source_type='file')

    # Define custom rules
    def has_subdomain(email):
        """Check if email has subdomain (more than 2 dots)."""
        domain = email.split('@')[1] if '@' in email else ''
        return domain.count('.') > 1

    def is_short_email(email):
        """Check if email is short (less than 20 characters)."""
        return len(email) < 20

    def has_plus_addressing(email):
        """Check if email uses plus addressing."""
        return '+' in email.split('@')[0] if '@' in email else False

    rules = {
        'subdomain': has_subdomain,
        'short': is_short_email,
        'plus_addressing': has_plus_addressing
    }

    categorized = engine.categorize(method='custom', rules=rules)

    print("\nCustom categorization results:")
    for category, emails in categorized.items():
        print(f"\n  {category}: ({len(emails)} emails)")
        for email in emails[:3]:
            print(f"    - {email}")


def demo_export():
    """Demonstrate exporting results."""
    print("\n" + "="*60)
    print("DEMO 7: Export Results")
    print("="*60)

    sample_file = Path(__file__).parent / "sample_data.txt"
    output_dir = Path(__file__).parent

    engine = EmailEngine()
    engine.extract(sample_file, source_type='file')
    engine.categorize(method='type')

    # Export to different formats
    json_file = output_dir / "output.json"
    csv_file = output_dir / "output.csv"
    txt_file = output_dir / "output.txt"

    engine.export_to_json(json_file, include_stats=True)
    engine.export_to_csv(csv_file, include_category=True)
    engine.export_to_txt(txt_file, grouped_by_category=True)

    print(f"\nExported results to:")
    print(f"  - JSON: {json_file}")
    print(f"  - CSV:  {csv_file}")
    print(f"  - TXT:  {txt_file}")


def demo_filtering():
    """Demonstrate filtering emails by category."""
    print("\n" + "="*60)
    print("DEMO 8: Filter Emails")
    print("="*60)

    sample_file = Path(__file__).parent / "sample_data.txt"

    engine = EmailEngine()
    engine.extract(sample_file, source_type='file')
    engine.categorize(method='type')

    # Filter to only free email providers
    free_emails = engine.filter_emails(categories=['free'])
    print(f"\nFree email providers ({len(free_emails)} total):")
    for email in free_emails[:5]:
        print(f"  - {email}")

    # Exclude free email providers
    non_free = engine.filter_emails(exclude_categories=['free'])
    print(f"\nNon-free emails ({len(non_free)} total):")
    for email in non_free[:5]:
        print(f"  - {email}")


def main():
    """Run all demos."""
    print("\n" + "="*60)
    print("Email Extraction and Categorization Engine - Demo")
    print("="*60)

    try:
        demo_basic_extraction()
        demo_file_extraction()
        demo_categorization_by_type()
        demo_categorization_by_domain()
        demo_keyword_categorization()
        demo_custom_rules()
        demo_export()
        demo_filtering()

        print("\n" + "="*60)
        print("All demos completed successfully!")
        print("="*60 + "\n")

    except Exception as e:
        print(f"\nError running demos: {e}")
        import traceback
        traceback.print_exc()
        return 1

    return 0


if __name__ == '__main__':
    sys.exit(main())
