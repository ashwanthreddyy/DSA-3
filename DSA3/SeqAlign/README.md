# SeqMatch - Local Sequence Alignment Tool (Smith-Waterman)

DSA-3 (25CS2103E) project. Finds the best matching local subsequences in two biological
sequences (DNA or protein), reports the optimal local alignment score, visualizes the alignment
and the scoring matrix, and lets you configure match / mismatch / gap scores.
Pure Java, standard library only (Swing GUI).

## Requirements
- JDK 17 or newer (`javac -version`)

## Build and run
```
cd SeqAlign
mkdir out
javac -d out src/*.java

java -cp out Main                       # GUI
java -cp out Tests                      # 27 test checks
java -cp out Main --cli TGTTACGG GGTTGACTA 3 -3 -2     # console mode: SEQ_A SEQ_B match mismatch gap
```

## Using the GUI
1. Paste two sequences (FASTA headers, spaces, lowercase and digits are cleaned automatically) or click *DNA Sample* / *Protein Sample*.
2. Set the Match (> 0), Mismatch (<= 0) and Gap (< 0) scores.
3. Click **Align**. Tabs:
   - **Alignment**: aligned subsequences with positions, colour-coded match / mismatch / gap, score and identity.
   - **Score Matrix Heatmap**: the DP matrix with the traceback path in red.
   - **Algorithm Comparison**: Smith-Waterman vs Needleman-Wunsch vs KMP on your input, plus complexity notes.
   - **Benchmark**: runtime vs sequence length chart.
4. *Save Result* writes the alignment and the comparison to a text file.

## Files
| File | Purpose |
|---|---|
| SmithWaterman.java | The algorithm: matrix fill, max-cell tracking, traceback |
| ScoringConfig.java | Configurable scoring parameters with validation |
| SequenceUtil.java | Input cleaning and DNA/protein validation |
| AlignmentResult.java | Result data and text formatting |
| NeedlemanWunsch.java, KMP.java | Baselines for the comparison (CO1) |
| Benchmark.java, AnalysisReport.java | Runtime measurement, comparison text |
| MainFrame, AlignmentPanel, HeatmapPanel, ChartPanel | Swing GUI and Java2D drawing |
| Main.java | Entry point (GUI / CLI) |
| Tests.java | Test cases with expected vs actual output |

## Algorithm
```
H[i][j] = max( 0,
               H[i-1][j-1] + s(a_i, b_j),    // match / mismatch
               H[i-1][j]   + gap,            // gap in B
               H[i][j-1]   + gap )           // gap in A
```
The best local alignment ends at the maximum cell of H; traceback follows the arrows back until a cell
with score 0. Time O(m*n), space O(m*n).

## Syllabus mapping
- **CO1**: problem-class signature (sequence alignment vs exact substring search) and strategy selection, shown in the Algorithm Comparison tab.
- **CO3**: advanced dynamic programming (local-alignment recurrence with reset-to-zero, traceback).
- **CO2 link**: KMP is used as the exact-matching baseline.
