#!/usr/bin/perl -w

# This will take the qualifying.txt file and split it
# into a number of pieces, based on how many lines you want
# in each file.

# Open the qualifier file
open (IN, "qualifying.txt");

$num = 1;
$splitnum = 20000;
$filenum = 1;

open (CURFILE, ">>qualifying_" . sprintf("%03s", $filenum) . ".txt");

while($inline = readline(IN)) {
	# Split time
	if($num >= $splitnum && substr($inline, -3, -2) eq ":") {
		close CURFILE;
		$filenum++;
		open (CURFILE, ">>qualifying_" . sprintf("%03s", $filenum) . ".txt");
		$num = 1;
	}

	print CURFILE $inline;

	$num++;
}

close CURFILE;
close IN;
