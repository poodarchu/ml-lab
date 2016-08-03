#!/usr/bin/perl
# Converts Netflix's probe.txt into a list of comma delimited rows:
#    mid, uid
# by tuladhaa
open (INPUT, "probe.txt") or die "Can't open probe data file.";
open (OUTPUT, ">probe2.txt") or die "Can't open probe2 for output.";
my $num = "";
while (<INPUT>)
{
    if (m/([0-9]+):/) {
	$num = $1;
	print "num: ".$num."\n";
    } else {
	print OUTPUT $num.",".$_;
    }
}
close INPUT;
close OUTPUT;
