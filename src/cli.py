#!/usr/bin/env python3
"""Command-line interface for the Email Extraction and Categorization Engine."""

import argparse
import sys
from pathlib import Path
from typing import Optional

from .engine import EmailEngine


def main():
    """Main CLI entry point."""
    parser = argparse.ArgumentParser(
        description='Email Extraction and Categorization Engine',
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  # Extract emails from text
  %(prog)s --text "Contact us at support@example.com or sales@example.org"

  # Extract from a file and categorize
  %(prog)s --file data.txt --categorize type --output results.json

  # Extract from directory and export to CSV
  %(prog)s --directory ./logs --categorize domain --output emails.csv --format csv

  # Extract with custom keywords categorization
  %(prog)s --file input.txt --categorize keywords --output sorted.txt --format txt
        """
    )

    # Input sources
    input_group = parser.add_mutually_exclusive_group(required=True)
    input_group.add_argument(
        '--text',
        type=str,
        help='Extract emails from text string'
    )
    input_group.add_argument(
        '--file',
        type=str,
        help='Extract emails from a file'
    )
    input_group.add_argument(
        '--directory',
        type=str,
        help='Extract emails from all files in a directory'
    )

    # Extraction options
    parser.add_argument(
        '--recursive',
        action='store_true',
        help='Search directory recursively (only with --directory)'
    )
    parser.add_argument(
        '--case-sensitive',
        action='store_true',
        help='Preserve email case (default: convert to lowercase)'
    )

    # Categorization options
    parser.add_argument(
        '--categorize',
        choices=['domain', 'type', 'keywords', 'none'],
        default='none',
        help='Categorization method to use (default: none)'
    )

    # Output options
    parser.add_argument(
        '--output',
        '-o',
        type=str,
        help='Output file path (if not specified, prints to stdout)'
    )
    parser.add_argument(
        '--format',
        choices=['json', 'csv', 'txt'],
        default='json',
        help='Output format (default: json)'
    )
    parser.add_argument(
        '--grouped',
        action='store_true',
        help='Group output by category (for txt format)'
    )
    parser.add_argument(
        '--stats',
        action='store_true',
        help='Include statistics in output'
    )

    # Display options
    parser.add_argument(
        '--verbose',
        '-v',
        action='store_true',
        help='Verbose output'
    )
    parser.add_argument(
        '--quiet',
        '-q',
        action='store_true',
        help='Suppress all output except results'
    )

    args = parser.parse_args()

    # Validate arguments
    if args.recursive and not args.directory:
        parser.error('--recursive can only be used with --directory')

    # Initialize engine
    engine = EmailEngine(case_sensitive=args.case_sensitive)

    try:
        # Extract emails
        if args.text:
            if args.verbose:
                print("Extracting emails from text...", file=sys.stderr)
            source = args.text
            source_type = 'text'
        elif args.file:
            if args.verbose:
                print(f"Extracting emails from file: {args.file}", file=sys.stderr)
            source = args.file
            source_type = 'file'
        elif args.directory:
            if args.verbose:
                mode = "recursively" if args.recursive else "non-recursively"
                print(
                    f"Extracting emails from directory {mode}: {args.directory}",
                    file=sys.stderr
                )
            source = args.directory
            source_type = 'directory'

        emails = engine.extract(source, source_type)

        if not emails:
            if not args.quiet:
                print("No emails found.", file=sys.stderr)
            return 0

        if args.verbose:
            print(f"Found {len(emails)} email(s)", file=sys.stderr)

        # Categorize if requested
        if args.categorize != 'none':
            if args.verbose:
                print(f"Categorizing emails by {args.categorize}...", file=sys.stderr)
            engine.categorize(method=args.categorize)

        # Output results
        if args.output:
            output_path = Path(args.output)

            if args.format == 'json':
                engine.export_to_json(output_path, include_stats=args.stats)
            elif args.format == 'csv':
                engine.export_to_csv(
                    output_path,
                    include_category=(args.categorize != 'none')
                )
            elif args.format == 'txt':
                engine.export_to_txt(
                    output_path,
                    grouped_by_category=args.grouped
                )

            if not args.quiet:
                print(f"Results saved to: {output_path}", file=sys.stderr)
        else:
            # Print to stdout
            if args.categorize != 'none':
                for category, category_emails in engine.categorized.items():
                    print(f"\n=== {category.upper()} ===")
                    for email in category_emails:
                        print(email)
            else:
                for email in emails:
                    print(email)

        # Print statistics if requested
        if args.stats and not args.quiet:
            summary = engine.get_summary()
            print("\n=== STATISTICS ===", file=sys.stderr)
            print(f"Total emails: {summary['total_emails']}", file=sys.stderr)
            if summary.get('statistics'):
                print("\nBy category:", file=sys.stderr)
                for category, count in summary['statistics'].items():
                    print(f"  {category}: {count}", file=sys.stderr)

        return 0

    except FileNotFoundError as e:
        print(f"Error: {e}", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"Error: {e}", file=sys.stderr)
        if args.verbose:
            import traceback
            traceback.print_exc(file=sys.stderr)
        return 1


if __name__ == '__main__':
    sys.exit(main())
