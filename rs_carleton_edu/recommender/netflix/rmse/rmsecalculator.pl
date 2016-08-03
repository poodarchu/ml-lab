#!/user/bin/perl

use warnings;
use strict;
use DBI;
our ($predicted_file, $database, $username, $password, $ratings_table);

sub init() {
    use Getopt::Std;
    my %arguments;
    getopts('f:d:u:p:t:q', \%arguments) or usage();

    $predicted_file = $arguments{f};
    $database = $arguments{d};
    $username = $arguments{u};
    $password = $arguments{p};
    $ratings_table = $arguments{t};

    usage() if (!$predicted_file || $predicted_file eq "");
    $database = "recommender" if (!$database);
    $username = "recommender" if (!$username);
    $password = "recommender" if (!$password);
    $ratings_table = "ratings" if (!$ratings_table);

    my $query = $arguments{q};
    if ($query) {
	my ($numValues, $rmse) = query_and_process_file();
	printf "%d pairs RMSE: %.5f\n", $numValues, $rmse;
    } else {
	my ($numValues, $rmse) = process_file();
	printf "%d pairs RMSE: %.5f\n", $numValues, $rmse;
    }
}

sub usage() {
    print "Usage:\n";
    print "perl rmsecalculator.pl -f predicted_ratings_file [-q [-d database] [-u username] [-p password] [-t ratings_table_name]]\n";
    print "  -q: Query the database to find actual ratings\n\n";
    exit(1);
}

sub process_file() {
    open (PREDICTED, $predicted_file) or die "Cannot open ".$predicted_file;
    my $numValues = 0;
    my $sumSquaredValues = 0;
    # Calculation code lifted from NetFlix's rmse.pl
    while(<PREDICTED>) {
	my ($rating,$prediction) = split(/\,/);
	my $delta = $rating - $prediction;
	$numValues++;
	$sumSquaredValues += $delta*$delta;
    }
    close(PREDICTED);
    return ($numValues, sqrt($sumSquaredValues/$numValues));
}

sub db_connect() {
    my $port = 3306;
    my $dbpath = "DBI:mysql:database=".$database.";host=localhost;port=".$port;
    return DBI->connect($dbpath, $username, $password) or die "Cannot connect to database.";
}

sub query_and_process_file() {
    open (PREDICTED, $predicted_file) or die "Cannot open ".$predicted_file;
    my $dbh = db_connect();
    my $static_stmt = "SELECT rating FROM ".$ratings_table." WHERE ";
    my $numValues = 0;
    my $sumSquaredValues = 0;

    while (<PREDICTED>) {
	my ($uid, $movie, $prediction) = split(/\,/);
	my $sq_stmt = $static_stmt."uid=".$uid." AND movie=".$movie.";";
	my $sth = $dbh->prepare($sq_stmt);
	my %row = $sth->fetchhash;
	my $real_rating = $row{"rating"};
	my $delta = $real_rating - $prediction;
	$numValues++;
	$sumSquaredValues += $delta*$delta;
    }
    close(PREDICTED);
    return ($numValues, sqrt($sumSquaredValues/$numValues));
}

init();
