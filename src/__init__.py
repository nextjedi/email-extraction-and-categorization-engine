"""Email Extraction and Categorization Engine

A powerful Python library for extracting and categorizing email addresses
from various sources.
"""

__version__ = "1.0.0"
__author__ = "Email Extraction Engine"

from .extractor import EmailExtractor
from .categorizer import EmailCategorizer
from .engine import EmailEngine

__all__ = ["EmailExtractor", "EmailCategorizer", "EmailEngine"]
