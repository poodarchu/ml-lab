#!/usr/bin/perl

use warnings;
use strict;
use DBI;
our ($database, $username, $password, $filespec, $tablename, $field_term, $line_term);
our ($sq, $dbh);
sub init() {
    use Getopt::Std;
    my %arguments;
    getopts('d:e:u:p:t:f:l:', \%arguments) or usage();

    $field_term = $arguments{e};
    $line_term = $arguments{l};
    $database = $arguments{d};
    $username = $arguments{u};
    $password = $arguments{p};
    $filespec = $arguments{f};
    $tablename = $arguments{t};

    # default values
    usage() if (!$tablename);
    $database = "recommender" if (!$database);
    $username = "recommender" if (!$username);
    $password = askpassword() if (!$password);
    usage() if (!$filespec);

    # This needs to happen only once
    $sq = "INTO TABLE ".$tablename;
    if ($field_term) {
	$sq = $sq." FIELDS TERMINATED BY '".$field_term."'";
    }
    if ($line_term) {
	$sq = $sq." LINES TERMINATED BY '".$line_term."'";
    }
}

sub askpassword() {
    print "\nEnter password: ";
    return <STDIN>;
}
sub usage() {
    print "Usage: perl upload.pl [-d database] [-f filespec] [-u user] [-p password] [-t table] [-e field_termination_string] [-l line_termination_string]\n";
    print " e.g.: perl upload.pl -d recommender -f ./data/flatfile.txt -u username -p passwd -t movies -e , -l \\n \n";
    exit(1);
}
sub db_connect {
    my $port = 3306;
    my $dbpath = "DBI:mysql:database=".$database.";host=localhost;port=".$port;
    $dbh = DBI->connect($dbpath, $username, $password) or die;
}

sub upload_file {
    print "Importing ".$filespec."...";
    my $sq_stmt = "LOAD DATA LOCAL INFILE '".$filespec."' ".$sq;
    print "SQL: ".$sq_stmt."\n";
    $dbh->do($sq_stmt) or die;
    print "done.\n";
}
init();
db_connect();
upload_file();



