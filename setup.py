"""Setup script for Email Extraction and Categorization Engine."""

from setuptools import setup, find_packages
from pathlib import Path

# Read the README file
this_directory = Path(__file__).parent
long_description = (this_directory / "README.md").read_text(encoding='utf-8')

setup(
    name='email-extraction-engine',
    version='1.0.0',
    author='Email Extraction Engine',
    description='A powerful Python library for extracting and categorizing email addresses',
    long_description=long_description,
    long_description_content_type='text/markdown',
    url='https://github.com/nextjedi/email-extraction-and-categorization-engine',
    packages=find_packages(exclude=['tests*', 'examples*']),
    classifiers=[
        'Development Status :: 4 - Beta',
        'Intended Audience :: Developers',
        'Topic :: Software Development :: Libraries :: Python Modules',
        'Topic :: Communications :: Email',
        'Topic :: Text Processing',
        'License :: OSI Approved :: MIT License',
        'Programming Language :: Python :: 3',
        'Programming Language :: Python :: 3.7',
        'Programming Language :: Python :: 3.8',
        'Programming Language :: Python :: 3.9',
        'Programming Language :: Python :: 3.10',
        'Programming Language :: Python :: 3.11',
    ],
    python_requires='>=3.7',
    install_requires=[
        # No external dependencies - uses only standard library
    ],
    extras_require={
        'dev': [
            'pytest>=7.0.0',
            'pytest-cov>=4.0.0',
        ],
    },
    entry_points={
        'console_scripts': [
            'email-engine=src.cli:main',
        ],
    },
    keywords='email extraction categorization parser text-processing',
    project_urls={
        'Bug Reports': 'https://github.com/nextjedi/email-extraction-and-categorization-engine/issues',
        'Source': 'https://github.com/nextjedi/email-extraction-and-categorization-engine',
    },
)
