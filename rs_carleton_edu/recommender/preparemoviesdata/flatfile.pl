#!/usr/bin/perl -w

# Open a new flat file
open (FLATFILE, ">>flatfile.txt");

# for each movie number (there are 17770), open the file,
# modify the line to include move number, then append to
# the flat file
for ($mnum = 1; $mnum <= 17770; $mnum += 1) {
    open (CURFILE, "mv_" . sprintf("%07s", $mnum) . ".txt");
    readline(CURFILE);
    while($line = readline(CURFILE)) {
	print FLATFILE $mnum . "," . $line;
    }
    close CURFILE;
}

close FLATFILE;
