Plsqldiff is a simple Java program to compare PL/SQL code 
ignoring differences in comments, whitespace and case in identifiers.
The differences can be shown graphically via a generated HTML file in the 
standard broser or in a normal window (Swing).

The return value of the program is 0 if no differences were found, and 1 if there are differences.
Other values than 0 and 1 indicate a program error.

usages:

-nooutput [file1 file2]
  no graphical output, only write a message to stdout and return 0 if the
  files are equivalent and 1 otherwise.
  This can be used for a commit hook in a version control system.

-gui [file1 file2]
  show differences with a swing gui

-html [file1 file2]
  show differences in a html file and open the file with the standard browser

[file1 file2]
  the same as -gui

If no arguments are supplied a dialog is openend and the user
has to select two files. The same as -gui.

Display of Changes
The program tokenizes the input files and then compares the tokens thereby
ignoring whitespace tokens,  comment tokens, case for identifiers and 
the difference between q'[..]' and normal string constants. The line number
of the tokens is ignored.
The edit distance is the Levenshtein distance: the number of insert and update 
operations on any of the two sequences to make them equal.
In the display the mismatched tokens are marked red. 
The matched tokens of the files are shown on the same line in the display.
But their respective columns is not changed. 
Therefor in some cases padding add the start of a line has to be 
added which is shown is gray spaces.
