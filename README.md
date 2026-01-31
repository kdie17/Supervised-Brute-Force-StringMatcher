# Supervised Brute Force String Matcher
This project is a research-based implementation of an enhanced string matching algorithm. It uses a Supervised Learning layer to guide the traditional Brute Force method, improving decision-making during the search process.

## Project Overview
Unlike standard algorithms that start searching immediately, this system includes a Training Phase. It "learns" the DNA of your target pattern—specifically its unique character set and its mathematical weight—before it ever touches the source text.

## Core Architecture: The Supervisor Guards
The system employs two specific guards to reduce unnecessary computational work:
-  The Vocabulary Guard: This guard uses the learned character set to immediately reject any window that begins with an "unknown" letter.
-  The Checksum Guard: This guard calculates the ASCII sum of the current text window. If the "weight" doesn't match the pattern, the supervisor blocks the expensive character-by-character comparison.

## Research Objectives
This project aims to:
-  Increase Efficiency: Reduce the total number of character comparisons.
-  Supervised Guidance: Use learned data to skip irrelevant text sections.
-  Accuracy Preservation: Maintain 100% exact matching reliability.

## Understanding the Results
When running trials, you will see two main metrics:
Control (Standard): The baseline performance of a "blind" search.
Experimental (Supervised): The performance of the system guided by the guards.
Note on Efficiency: If the Efficiency Boost is negative, it indicates Overhead Dominance. This means the pattern was too short to justify the "cost" of the supervisor's thinking time.

## How to Use
1. Source Text: Paste your dataset here.
2. Target Pattern: Enter the word you are looking for.
3. Analyze: Click "Train & Execute" to see the "Learned Information" in action.
4. Interpretation: Read the bottom report for a plain-English breakdown of how many checks the guards blocked.
