#!/usr/bin/perl

=head1 NAME

classify.pl

=head1 SYNOPSIS

Medical Challenge 2007 ICD9CM classification program from radiology reports.

=head1 USAGE

classify.pl [OPTIONS] DESTINATION SOURCE

=head1 INPUT

The training data from the 2007 Medical Challenge.

=head2 Required Arguments:

=head3 DESTINATION

The data file where the accuracy information will be stored
if running a simulation 

OR

The data file where the xml formatted test data will be stored 
if running the Challenge submission.

=head2 Optional Arguments:

=head3 --simulation NUMBER

The number of simulation experiments to run the program on the 
training data. Default: 1000

NOTE: If the --test option is set the simulation will only give 
one iteration

=head3 --split NUMBER

The percentage of test data that should be randomly extracted 
from the training data at each iteration of the simulations.
Default: 25

=head3 --test FILE

The program will train on the complete training data and test on 
the give test data. The simulations for this case will on be 
of one iteration.

=head3 --nocodes

Whether the test file contains the ICD9CM codes

=head3 --help

Displays the summary of command line options.

=head3 --version

Displays the version information.

=head1 SYSTEM REQUIREMENTS

=item * Perl (version 5.8.5 or better) - http://www.perl.org

=item * WEKA data mining package - http://sourceforge.net/projects/weka/

=head1 AUTHOR

Bridget McInnes, University of Minnesota

=head1 COPYRIGHT

Copyright (c) 2007,

 Ted Pedersen, University of Minnesota, Duluth.
 tpederse at umn.edu

 Bridget McInnes, University of Minnesota
 bthomson at cs.umn.edu

 John Carlis, University of Minnesota
 carlis at cs.umn.edu

 Serguei Pakhomov, University of Minnesota
 pakh0002 at umn.edu

This program is free software; you can redistribute it and/or modify it under
the terms of the GNU General Public License as published by the Free Software
Foundation; either version 2 of the License, or (at your option) any later
version.

This program is distributed in the hope that it will be useful, but WITHOUT
ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
FOR A PARTICULAR PURPOSE. See the GNU General Public License for more details.

You should have received a copy of the GNU General Public License along with
this program; if not, write to

 The Free Software Foundation, Inc.,
 59 Temple Place - Suite 330,
 Boston, MA  02111-1307, USA.

=cut

use Getopt::Long;

GetOptions( "version", "help", "simulation=s", "split=s", "test=s", "nocodes");

#  if help is defined, print out help
if( defined $opt_help ) {
    $opt_help = 1;
    &showHelp();
    exit;
}

#  if version is requested, show version
if( defined $opt_version ) {
    $opt_version = 1;
    &showVersion();
    exit;
}

#  retrieve the destination file
my $destination = shift;


#  check that destination has been supplied
if( !($destination) ) {
    print STDERR "No output file (DESTINATION) supplied.\n";
    askHelp();
    exit;
}

#  open the destination file
open(DST, ">$destination") || die "Could not open (DESTINATION) file: $destination\n";

#  get the number of simulations
if( !(defined $opt_simulation) ) {
    $opt_simulation = 1000;
}

#  get the split for training and test
if( !(defined $opt_split) ) {
    $opt_split = 25;
}

#  check if test data is given
if( defined $opt_test ) {
    $opt_simulation = 1;
}
    
my $num             = 0;
my $simulationCount = 0;
my @accuracy        = ();

my $split           = $opt_split;
my $trainingfile    = shift;
    
while($simulationCount < $opt_simulation) {
    
    $simulationCount++;
    
    open(TFILE, $trainingfile) || die;
    
    my $test = "";
    if(! (defined $opt_test) ) {
	$test = "test";
    } else { $test = $opt_test; }
    
    my $train = "train";
    my $unigram = "unigrams";

    #  open the relevant documents
    if(! (defined $opt_test) ) {
	open(TEST, ">$test")   || die "Could not open test file\n";
    }
    open(TRAIN, ">$train")     || die "Could not open train file\n";
    open(UNIGRAM, ">$unigram") || die "Could not open unigram file\n";
    
    
    #  get the training data information
    my %hash = ();
    my $doc = "";
    while(<TFILE>) {
	if($_=~/\<code origin\=\"COMPANY/) { next; }
	$doc .= $_;
	
	if($_=~/<\/doc>/) {
	    $hash{$doc} = rand();
	    $doc = "";
	}
    }

    #  initialize the unigrams
    my %unigrams = ();
    
    #  get the split percentage
    my $total = keys %hash;
    my $stop = 0;
    if(! (defined $opt_test) ) {
	$stop  = $total * ($split/100);
    } else { $stop = $total + 1; }

    if(! (defined $opt_test)) {
	print TEST "<?xml version='1.0' standalone='yes'?>\n";
	print TEST "<docs>\n";
    }
    
    print TRAIN "<?xml version='1.0' standalone='yes'?>\n";
    print TRAIN "<docs>\n";
    
    my $counter = 0;
    foreach (sort {$hash{$b}<=>$hash{$a}} keys %hash) {
	
	$_=~s/\<\?xml version\=\'1\.0\' standalone\=\'yes\'\?\>//g;
	$_=~s/\<docs\>//g;
	
	if($counter <= $stop and !(defined $opt_test)) {
	    print TEST "$_";
	}
	else {
	    
	    $_=~/\<doc id=\"(.*)\" type/;
	    $id = $1;
	    
	    $_=~/\<text origin\=\"CCHMC_RADIOLOGY\" type\=\"CLINICAL_HISTORY\"\>(.*)\<\/text\>/;
	    $history = lc($1);
	    $history=~s/[\.\,\?\'\"\`\)\(]//g; 
	    $history=~s/\s-\s//g;
	    
	    $_=~/\<text origin\=\"CCHMC_RADIOLOGY\" type\=\"IMPRESSION\"\>(.*)\<\/text\>/;
	    $impression = lc($1);
	    $impression=~s/[\.\,\?\'\"\`\)\(]//g; 
	    $impression=~s/\s-\s//g;

	    my @h_array = split/\s+/, $history;
	    my @i_array = split/\s+/, $impression;
	    
	    my @codes = ();

	    while($_=~/<code origin=\"CMC_MAJORITY\" type=\"ICD\-9\-CM\"\>(.*)\<\/code\>/g) {
		push @codes, $1; 
	    }
	    
	    my $code = "";
	    foreach (@codes) {
		$code .= $_ . "_";
	    } chop $code; 
	    
	    foreach my $word (@h_array) { $unigram{$code}{$word}++; }
	    foreach my $word (@i_array) { $unigram{$code}{$word}++; }
	    
	    print TRAIN "$_";
	}
	$counter++;
    }
    if(! (defined $opt_test) ) {
	print TEST "<\/docs>\n";
    }
    
    print TRAIN "<\/docs>\n";
    
    foreach my $code (sort keys %unigram) {
	foreach my $word (sort keys %{$unigram{$code}}) {
	    print UNIGRAM "$code : $word : $unigram{$code}{$word}\n";
	}
    }
    
    close TEST;
    close TRAIN;
    close UNIGRAM;

    
    print "Convert XML formated data to ARFF format\n";
    if(defined $opt_nocodes) {
	system("perl bin/xml2arff.pl --train $train --test $test --nocodes --unigram unigrams --cutoff 2 --overlap optionfiles/codes-text output");
    }
    else {
	system("perl bin/xml2arff.pl --train $train --test $test --unigram unigrams --cutoff 2 --overlap optionfiles/codes-text output");
    }

    print "Run WEKA on the ARFF file\n";
    system("java weka.classifiers.bayes.NaiveBayes -t output.train.arff -T output.test.arff -p 0 > output.test.answers");
    
    print "Convert WEKA arff output to XML format\n";
    system("perl bin/arff2xml.pl --testFile $test --answerFile output.test.answers --orderFile output.test.order output.test.xml");

    if(defined $opt_test and defined $opt_nocodes) {
	
	system("rm output.test.answers output.test.arff output.test.order");
	system("rm output.train.arff train unigrams out");
	
	system("mv output.test.xml $destination");
	
	print "\n\n";
	print "The output file containing the test data\n";
	print "in xml format with the codes assigned by\n";
	print "the weka data mining package are in file:\n\n";
	print "    $destination\n\n";
	
	exit;
    }
    
    print "  Run main ranking program\n";
    system ("perl bin/main_ranking.pl test output.test.xml >fscore");
    
    open(SCORE, "fscore") || die "Could not open fscore\n";
    
    while(<SCORE>) { 
	chomp; 
	$_=~/F\-1 measure (0\.[0-9]+)/; 
	my $fmeas = $1; 
	push @accuracy, $fmeas;
	$num += $fmeas; 
	print DST "$simulationCount : $fmeas\n";
    }
}

my $mean = $num/$simulationCount;

my $sum = 0;
foreach my $x (@accuracy) {
    $sum += ( ($x - $mean)**2 );
}

my @sorted = sort @accuracy;
my $per = ($#sorted+1)*.025;
my $lower = int($per);
my $upper = int($#sorted-$per);
my $st = sqrt( (1/$simulationCount) * $sum );

print DST "OVERALL ACCURACY       : $mean\n";
print DST "STANDARD DEVIATION     : $st\n";
print DST "LOWER  2.5% CONFIDENCE : $sorted[$lower]\n";
print DST "UPPER 2.5% CONFIDENCE  : $sorted[$upper]\n"; 

print "\n\n";
print "The output file containing the test data\n";
print "in xml format with the codes assigned by\n";
print "the weka data mining package are in file:\n\n";
print "    output.test.xml\n\n";

print "The simulation results are located in:\n\n";
print "     $destination\n\n";

system("rm output.test.answers output.test.arff output.test.order");
system("rm output.train.arff train unigrams test fscore");

##############################################################################
#  SUB FUNCTIONS
##############################################################################

#  function to output minimal usage notes
sub minimalUsageNotes {
    
    print STDERR "Usage: classify.pl [OPTIONS] DESTINATION SOURCE\n";
    askHelp();
}

#  function to output help messages for this program
sub showHelp() {

    print "Usage: classify.pl [OPTIONS] SOURCE\n\n";
    
    print "This is a wrapper program to classify ICD9CM Codes for the \n";
    print "2007 Medical Challenge. For more information on the challenge\n";
    print "see their website: \n\n";
    print "   http://www.computationalmedicine.org/challenge/index.php\n\n";


    print "OPTIONS:\n\n";

    print "--simulation NUMBER      The number of simulation experiments to \n";
    print "                         run the program on the training data. \n";
    print "                         Default: 1000\n\n";

    print "--split NUMBER           The percentage of test data that should be \n";
    print "                         randomly extracted from the training data at \n";
    print "                         each iteration of the simulations. Default: 25\n\n";

    print "--test FILE              The program wil train on the complete training\n";
    print "                         data and test on the given test data. \n\n";
    
    print "--nocodes                The test file does not contain the ICD9CM Codes\.n\n";

    print "--version                Prints the version number.\n\n";
 
    print "--help                   Prints this help message.\n\n";
}

#  function to output the version number
sub showVersion {
    print '$Id: classify.pl,v 1.5 2007/05/16 16:31:09 btmcinnes Exp $';
    print "\nCopyright (c) 2007, Ted Pedersen, Bridget McInnes, \n";
    print "John Carlis, and Serguei Pakhomov\n";
}

#  function to output "ask for help" message when user's goofed
sub askHelp {
    print STDERR "Type classify.pl --help for help.\n";
}
    
