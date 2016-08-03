open (INPUT, "movie_titles.txt") or die "Can't open movies titles file.";
open (OUTPUT, ">movie_titles2.txt") or die "Can't open movies titles 2 file for output";
while (<INPUT>) {
    s/([A-Z]+)(,{1})/\1;/gi;
    print OUTPUT $_;
}
close(INPUT);
close(OUTPUT);
