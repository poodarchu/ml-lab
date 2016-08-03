#!/usr/bin/perl

use warnings;
use strict;
use DBI;
our ($database, $username, $password, $dirspec, $tablename, $field_term, $line_term);
our ($sq, $dbh);
sub init() {
    use Getopt::Std;
    my %arguments;
    getopts('a:d:u:p:t:f:l:', \%arguments) or usage();

    $field_term = $arguments{f};
    $line_term = $arguments{l};
    $database = $arguments{a};
    $username = $arguments{u};
    $password = $arguments{p};
    $dirspec = $arguments{d};
    $tablename = $arguments{t};

    # default values
    usage() if ($tablename eq "");
    $database = "tuladhaa" if (!$database);
    $username = "tuladhaa" if (!$username);
    $password = "bl4ckb3rr4" if (!$password);
    $dirspec = "." if (!$dirspec);

    # This needs to happen only once
    $sq = "INTO TABLE ".$tablename;
    if ($field_term) {
	$sq = $sq." FIELDS TERMINATED BY '".$field_term."'";
    }
    if ($line_term) {
	$sq = $sq." LINES TERMINATED BY '".$line_term."'";
    }
}

sub usage() {
    print "Usage: perl upload.pl [-a database] [-d directory] [-u user] [-p password] [-t table] [-e field_termination_string] [-l line_termination_string]\n";
    print " e.g.: perl upload.pl -a recommender -d ./data -u username -p passwd -t movies -e , -l \\n \n";
    exit(1);
}
sub db_connect {
    my $port = 3306;
    my $dbpath = "DBI:mysql:database=".$database.";host=localhost;port=".$port;
    $dbh = DBI->connect($dbpath, $username, $password) or die;
}

sub process_files {
    use File::Find;
    find(\&upload_file, $dirspec);
}
sub upload_file {
    return unless /\.txt$/;
    print "Importing ".$File::Find::name."...";
    my $sq_stmt = "LOAD DATA LOCAL INFILE '".$File::Find::name."' ".$sq;
    print "SQL: ".$sq_stmt."\n";
    $dbh->do($sq_stmt) or die;
    print "done.\n";
}
init();
db_connect();
process_files();



