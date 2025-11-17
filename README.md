# Email Extraction and Categorization Engine

A powerful and flexible Python library for extracting and categorizing email addresses from various sources. This engine provides comprehensive tools for parsing text, files, and directories to find email addresses, then categorizing them based on multiple criteria.

## Features

### Email Extraction
- **Multiple Source Types**: Extract from text strings, files, multiple files, or entire directories
- **Robust Regex Pattern**: RFC 5322 compliant email pattern matching
- **Duplicate Removal**: Automatically removes duplicate emails while preserving order
- **Case Sensitivity**: Optional case-sensitive or case-insensitive extraction
- **Recursive Directory Search**: Search through directory trees
- **File Extension Filtering**: Process only specific file types
- **Email Validation**: Built-in email address validation
- **Extraction Cache**: Track all emails extracted during a session

### Email Categorization
- **By Domain**: Group emails by their domain names
- **By Type**: Categorize as free, business, educational, government, or other
- **By Keywords**: Categorize based on keywords in email addresses
- **By Pattern**: Use regex patterns for custom categorization
- **Custom Rules**: Define your own categorization functions
- **Multiple Methods**: Apply multiple categorization methods simultaneously
- **Statistics**: Get counts and breakdowns of categorized emails
- **Filtering**: Filter or exclude specific categories

### Export & Integration
- **JSON Export**: Export with full categorization and statistics
- **CSV Export**: Create spreadsheet-compatible output
- **Text Export**: Generate human-readable text files
- **Command-Line Interface**: Full-featured CLI for easy automation
- **Python API**: Clean, intuitive API for programmatic use
- **Summary Reports**: Generate statistical summaries

## Installation

### From Source
```bash
git clone https://github.com/nextjedi/email-extraction-and-categorization-engine.git
cd email-extraction-and-categorization-engine
pip install -e .
```

### Development Installation
```bash
pip install -e .[dev]
```

## Quick Start

### Python API

#### Basic Extraction
```python
from src.engine import EmailEngine

# Create engine
engine = EmailEngine()

# Extract from text
text = "Contact us at support@company.com or sales@company.com"
emails = engine.extract(text, source_type='text')
print(emails)  # ['support@company.com', 'sales@company.com']
```

#### Extract and Categorize
```python
# Extract from file and categorize by type
result = engine.process(
    'data.txt',
    source_type='file',
    categorization_method='type'
)

print(f"Total emails: {result['total_emails']}")
print(f"Statistics: {result['statistics']}")
print(f"Free emails: {result['categorized']['free']}")
```

#### Custom Categorization
```python
# Define custom rule
def is_admin_email(email):
    return 'admin' in email or 'root' in email

# Categorize with custom rules
engine.extract('emails.txt', source_type='file')
categorized = engine.categorize(
    method='custom',
    rules={'admin': is_admin_email}
)
```

#### Export Results
```python
# Export to different formats
engine.export_to_json('results.json', include_stats=True)
engine.export_to_csv('results.csv', include_category=True)
engine.export_to_txt('results.txt', grouped_by_category=True)
```

### Command-Line Interface

#### Extract from Text
```bash
python -m src.cli --text "Email me at user@example.com"
```

#### Extract from File and Categorize
```bash
python -m src.cli --file data.txt --categorize type --output results.json
```

#### Extract from Directory
```bash
python -m src.cli --directory ./logs --recursive --categorize domain --output emails.csv --format csv
```

#### With Statistics
```bash
python -m src.cli --file input.txt --categorize type --stats --output results.json
```

## Usage Examples

### Example 1: Extract from Multiple Files
```python
from src.engine import EmailEngine

engine = EmailEngine()
files = ['file1.txt', 'file2.txt', 'file3.log']
emails = engine.extract(files, source_type='files')
print(f"Found {len(emails)} unique emails across all files")
```

### Example 2: Categorize by Domain
```python
from src.engine import EmailEngine

engine = EmailEngine()
engine.extract('contacts.txt', source_type='file')
categorized = engine.categorize(method='domain')

for domain, emails in categorized.items():
    print(f"\n{domain}: {len(emails)} emails")
    for email in emails:
        print(f"  - {email}")
```

### Example 3: Keyword-Based Categorization
```python
from src.engine import EmailEngine

engine = EmailEngine()
engine.extract('emails.txt', source_type='file')

keywords = {
    'sales': ['sales', 'business', 'enterprise'],
    'support': ['support', 'help', 'service'],
    'admin': ['admin', 'root', 'system']
}

categorized = engine.categorize(method='keywords', keywords=keywords)
```

### Example 4: Filter and Export
```python
from src.engine import EmailEngine

engine = EmailEngine()
engine.extract('all_emails.txt', source_type='file')
engine.categorize(method='type')

# Get only business emails
business_emails = engine.filter_emails(categories=['business', 'other'])

# Export filtered results
with open('business_only.txt', 'w') as f:
    for email in business_emails:
        f.write(f"{email}\n")
```

### Example 5: Extract from Directory
```python
from src.engine import EmailEngine

engine = EmailEngine()
emails = engine.extract(
    './documents',
    source_type='directory'
)

# Get summary
summary = engine.get_summary()
print(f"Extracted {summary['total_emails']} emails from directory")
```

## API Reference

### EmailEngine

Main engine class that integrates extraction and categorization.

#### Methods

- `extract(source, source_type)`: Extract emails from a source
- `categorize(method, **kwargs)`: Categorize extracted emails
- `process(source, source_type, categorization_method, **kwargs)`: Extract and categorize in one step
- `export_to_json(output_path, include_stats)`: Export to JSON
- `export_to_csv(output_path, include_category)`: Export to CSV
- `export_to_txt(output_path, grouped_by_category)`: Export to text
- `get_summary()`: Get extraction/categorization summary
- `filter_emails(categories, exclude_categories)`: Filter by category
- `reset()`: Clear all data

### EmailExtractor

Handles email extraction from various sources.

#### Methods

- `extract_from_text(text)`: Extract from string
- `extract_from_file(file_path)`: Extract from file
- `extract_from_files(file_paths)`: Extract from multiple files
- `extract_from_directory(directory_path, recursive, file_extensions)`: Extract from directory
- `validate_email(email)`: Validate email format
- `get_all_extracted()`: Get all cached emails
- `clear_cache()`: Clear extraction cache

### EmailCategorizer

Categorizes emails using various methods.

#### Methods

- `categorize_by_domain(emails)`: Group by domain
- `categorize_by_type(emails)`: Categorize by type (free/business/edu/gov)
- `categorize_by_keywords(emails, keywords)`: Categorize by keywords
- `categorize_by_pattern(emails, patterns)`: Categorize by regex patterns
- `categorize_by_custom_rule(emails, rules)`: Categorize with custom functions
- `get_statistics(categorized)`: Get category counts
- `filter_by_category(categorized, categories)`: Filter to specific categories
- `exclude_categories(categorized, categories)`: Exclude specific categories

## Categorization Methods

### By Type
Categorizes emails into:
- **free**: Gmail, Yahoo, Hotmail, etc.
- **business**: Corporate domains
- **educational**: .edu domains
- **government**: .gov domains
- **other**: Everything else

### By Domain
Groups emails by their domain name (e.g., all @company.com emails together)

### By Keywords
Categorize based on keywords in the email address:
```python
keywords = {
    'sales': ['sales', 'business'],
    'support': ['support', 'help']
}
```

### By Pattern
Use regex patterns:
```python
patterns = {
    'admin': r'admin.*@.*',
    'numeric': r'.*\d+.*@.*'
}
```

### Custom Rules
Define your own categorization logic:
```python
def is_vip(email):
    return email in vip_list

rules = {'vip': is_vip}
```

## Testing

Run the test suite:

```bash
# Run all tests
python -m pytest tests/

# Run with coverage
python -m pytest tests/ --cov=src --cov-report=html

# Run specific test file
python -m pytest tests/test_extractor.py -v

# Run specific test
python -m pytest tests/test_extractor.py::TestEmailExtractor::test_extract_from_text -v
```

## Examples

Run the demo script to see all features in action:

```bash
python examples/demo.py
```

The demo script demonstrates:
1. Basic email extraction
2. Extracting from files
3. Categorization by type
4. Categorization by domain
5. Keyword-based categorization
6. Custom categorization rules
7. Exporting results
8. Filtering emails

## Project Structure

```
email-extraction-and-categorization-engine/
├── src/
│   ├── __init__.py          # Package initialization
│   ├── extractor.py         # Email extraction logic
│   ├── categorizer.py       # Email categorization logic
│   ├── engine.py            # Main engine (combines extraction & categorization)
│   └── cli.py               # Command-line interface
├── tests/
│   ├── __init__.py
│   ├── test_extractor.py    # Tests for extractor
│   ├── test_categorizer.py  # Tests for categorizer
│   └── test_engine.py       # Tests for engine
├── examples/
│   ├── sample_data.txt      # Sample email data
│   └── demo.py              # Demonstration script
├── requirements.txt         # Python dependencies
├── setup.py                 # Package setup
└── README.md                # This file
```

## Requirements

- Python 3.7 or higher
- No external dependencies (uses only standard library)

## License

MIT License

## Contributing

Contributions are welcome! Please feel free to submit a Pull Request.

## Support

For issues, questions, or contributions, please open an issue on GitHub.

## Changelog

### Version 1.0.0
- Initial release
- Email extraction from text, files, and directories
- Multiple categorization methods (domain, type, keywords, pattern, custom)
- Export to JSON, CSV, and TXT formats
- Command-line interface
- Comprehensive test suite
- Full documentation and examples
