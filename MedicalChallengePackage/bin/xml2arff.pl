#!/usr/bin/perl

=head1 NAME

xml2arff.pl

=head1 SYNOPSIS

The program takes training and test data in the Medical Challenge 2007 
xml format. It extract user defined features and outputs the 
information in .arff format for the WEKA data mining package.

=head1 USAGE

xml2arff.pl [OPTIONS] PREFIX

=head2 Required Arguments:

=head3 PREFIX

The prefix of the files created for this program.

=head3 --train

The training data in the 2007 Medical Challenge xml format.

=head3 --test 

The test data in the 2007 Medical Challenge xml format.

=head2 Optional Arguments:

=head3 --bow

This option stand for bag of words. It is the words 
in the report.

=head3 --unigram

Highly frequency words in the report.

The default cutoff is zero but the --cutoff option (see below) 
allows a frequency threshold to be established. The frequency 
thresholds are obtained by counting the number of times a word 
is seen with the ICD9CM code.data.

    For example, if we set a cutoff of five (--cutoff 5) then 
    any word that has been seen in a report with that ICD9CM 
    code at least five times will be included.



=head3 --cutoff NUMBER
    
The cutoff option can be used in conjunction with the --unigram 
option to establish a frequency threshold.

    For example, if we set a cutoff of NUMBER (--cutoff NUMBER) 
    then any surrounding unigram that has been seen in a report 
    with a specified ICD9CM code at least NUMBER of times will 
    be included in the feature set otherwise it will not.


=head3 --gender

Whether the radiology report is referring to a female or male.
This is based of looking for gender specific words.

=head3 --overlap FILE

Whether the text descriptions of the ICD9CM codes are in the 
text of the report. The descriptions are in a user defined 
file.

=head3 --tags

Whether the following words exist in the report:
1. after
2. history
3. prior
4. previous
5. status post
6. s/p

=head3 --nocodes

The test data does not have the associated ICD9CM codes.

=head3 --help

Displays the summary of command line options.

=head3 --version

Displays the version information.

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

my $testSentenceCount   = 0;
my $testSentenceLength  = 0;
my $trainSentenceCount  = 0;
my $trainSentenceLength = 0;
my @trainSentence = ();
my @testSentence = ();


GetOptions( "version", "help", "bow", "unigram=s", "cutoff=s", "gender", "overlap=s", "train=s", "test=s", "tags", "nocodes");

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

# make certain the test and training files are defined
if( !(defined $opt_train) or !(defined $opt_test) ) {
    print "The TRAIN and TEST files must be specified.\n";
}

$test_id           = "$destination.test.order";
$test_destination  = "$destination.test.arff";
$train_destination = "$destination.train.arff";

#  check to see if destination exists, and if so, if we should overwrite
if( -e $test_destination ) {
    print "Output test file $test_destination already exists! Overwrite (Y/N)?";
    my $reply = <STDIN>;  chomp $reply; $reply = uc($reply);
    exit 0 if ($reply ne "Y"); 
}

#  check to see if destination exists, and if so, if we should overwrite
if( -e $train_destination ) {
    print "Output train file $train_destination already exists! Overwrite (Y/N)?";
    my $reply = <STDIN>;  chomp $reply; $reply = uc($reply);
    exit 0 if ($reply ne "Y"); 
}

open(TEST_ID,     ">$test_id") || die "Could not open TEST ID file: $test_id\n";
open(TEST_DST,    ">$test_destination") || die "Could not open TEST DST file: $test_destination\n";
open(TRAIN_DST,    ">$train_destination") || die "Could not open TRAIN DST file: $train_destination\n";


my $cutoff = 0;
my %unigramHash = ();
my %overlapHash = ();
my %genderHash  = ();

checkUnigram();
checkOverlap();

my $BagOfWordsCounter  = 0;
my $UnigramCounter     = 0;
my $OverlapCounter     = 0;

my %BagOfWordsKey      = ();
my %UnigramKey         = ();
my %OverlapKey         = ();

my %BagOfWordsHash     = ();
my %UnigramHash        = ();
my %OverlapHash        = ();

my %tagAfter      = ();
my %tagHistory    = ();
my %tagPrior      = ();
my %tagPrevious   = ();
my %tagStatusPost = ();
my %tagSP         = ();
my %tagPost       = ();

my $id = "";
my $id_code = "";

my %icd9cmHash  = ();
my %icd9cmCodes = ();
my %testCodes   = ();

my $idCounter = 0;


getTrainFeatures($opt_train);
getTestFeatures();

my $avgTestSent  = $testSentenceLength/$testSentenceCount;
my $avgTrainSent = $trainSentenceLength/$trainSentenceCount;

my $trainCount = $#trainSentence + 1;
@trainSentence = sort { $a <=> $b } @trainSentence; 

my $trainMedian = 0;
if ($trainCount % 2) { 
 $trainMedian = $trainSentence[int($trainCount/2)]; 
} else { 
 $trainMedian = ($trainSentence[$trainCount/2] + $trainSentence[$trainCount/2 - 1]) / 2; 
}

my $testCount = $#testSentence + 1;
@testSentence = sort { $a <=> $b } @testSentence; 
if ($testCount % 2) { 
 $testMedian = $testSentence[int($testCount/2)]; 
} else { 
 $testMedian = ($testSentence[$testCount/2] + $testSentence[$testCount/2 - 1]) / 2; 
}

print TEST_DST "\@RELATION codes\n";
print TRAIN_DST "\@RELATION codes\n";

if(defined $opt_tags) {

    print TEST_DST  "\@ATTRIBUTE after_tag NUMERIC\n";
    print TRAIN_DST "\@ATTRIBUTE after_tag NUMERIC\n";

    print TEST_DST  "\@ATTRIBUTE history_tag NUMERIC\n";
    print TRAIN_DST "\@ATTRIBUTE history_tag NUMERIC\n";

    print TEST_DST  "\@ATTRIBUTE prior_tag NUMERIC\n";
    print TRAIN_DST "\@ATTRIBUTE prior_tag NUMERIC\n";

    print TEST_DST  "\@ATTRIBUTE previous_tag NUMERIC\n";
    print TRAIN_DST "\@ATTRIBUTE previous_tag NUMERIC\n";

    print TEST_DST  "\@ATTRIBUTE statuspost_tag NUMERIC\n";
    print TRAIN_DST "\@ATTRIBUTE statuspost_tag NUMERIC\n";

    print TEST_DST  "\@ATTRIBUTE sp_tag NUMERIC\n";
    print TRAIN_DST "\@ATTRIBUTE sp_tag NUMERIC\n";

    print TEST_DST  "\@ATTRIBUTE post_tag NUMERIC\n";
    print TRAIN_DST "\@ATTRIBUTE post_tag NUMERIC\n";
}    
if(defined $opt_bow) {
    foreach (sort {$BagOfWordsKey{$a}<=>$BagOfWordsKey{$b}} keys %BagOfWordsKey) {
	print TEST_DST "\@ATTRIBUTE $_ NUMERIC\n";
	print TRAIN_DST "\@ATTRIBUTE $_ NUMERIC\n";
    }
}
if(defined $opt_overlap) {
    foreach (sort {$OverlapKey{$a}<=>$OverlapKey{$b}} keys %OverlapKey) {
	print TEST_DST "\@ATTRIBUTE $_ NUMERIC\n";
	print TRAIN_DST "\@ATTRIBUTE $_ NUMERIC\n";
    }
}

if(defined $opt_unigram) {
    foreach (sort {$UnigramKey{$a}<=>$UnigramKey{$b}} keys %UnigramKey) {
	print TEST_DST "\@ATTRIBUTE $_ NUMERIC\n";
	print TRAIN_DST "\@ATTRIBUTE $_ NUMERIC\n";
    }
}

if(defined $opt_gender) {
    print TEST_DST "\@ATTRIBUTE GENDER {female,male,none}\n";
    print TRAIN_DST "\@ATTRIBUTE GENDER {female,male,none}\n";
}

my $codes = "";
foreach (sort keys %icd9cmCodes) { 
    if($_=~/\?/) { next; }
    if($_=~/testCode/) { next; }
    $codes .= $_ . ",";
} chop $codes;

print TEST_DST "\@ATTRIBUTE ICD9CM {$codes}\n";
print TRAIN_DST "\@ATTRIBUTE ICD9CM {$codes}\n";

print TRAIN_DST "\@DATA\n";
print TEST_DST "\@DATA\n";

foreach my $id (sort keys %icd9cmHash) {

    my $train = 1;
    if(exists $testCodes{$id}) { $train = 0; }

    if($train == 0) {
	print TEST_ID "$id\n";
    }

    if(defined $opt_tags){
	if($train == 1) {
	    exists $tagAfter{$id}      ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    exists $tagHistory{$id}    ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    exists $tagPrior{$id}      ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    exists $tagPrevious{$id}   ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    exists $tagStatusPost{$id} ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    exists $tagSP{$id}         ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    exists $tagPost{$id}       ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	}
	else {
	    exists $tagAfter{$id}      ? print TEST_DST "1," : print TEST_DST "0,";
	    exists $tagHistory{$id}    ? print TEST_DST "1," : print TEST_DST "0,";
	    exists $tagPrior{$id}      ? print TEST_DST "1," : print TEST_DST "0,";
	    exists $tagPrevious{$id}   ? print TEST_DST "1," : print TEST_DST "0,";
	    exists $tagStatusPost{$id} ? print TEST_DST "1," : print TEST_DST "0,";
	    exists $tagSP{$id}         ? print TEST_DST "1," : print TEST_DST "0,";
	    exists $tagPost{$id}       ? print TEST_DST "1," : print TEST_DST "0,";
	}
    }
    
    if(defined $opt_bow) {
	if($train == 1) {
	    foreach (sort {$BagOfWordsKey{$a}<=>$BagOfWordsKey{$b}} keys %BagOfWordsKey) {
		exists ${$BagOfWordsHash{$id}}{$_} ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    }
	}
	else {
	    foreach (sort {$BagOfWordsKey{$a}<=>$BagOfWordsKey{$b}} keys %BagOfWordsKey) {
		exists ${$BagOfWordsHash{$id}}{$_} ? print TEST_DST "1," : print TEST_DST "0,";
	    }
	}
    }
    if(defined $opt_overlap) {
	if($train == 1) {
	    foreach (sort {$OverlapKey{$a}<=>$OverlapKey{$b}} keys %OverlapKey) {
		exists ${$OverlapHash{$id}}{$_} ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    }
	}
	else {
	    foreach (sort {$OverlapKey{$a}<=>$OverlapKey{$b}} keys %OverlapKey) {
		exists ${$OverlapHash{$id}}{$_} ? print TEST_DST "1," : print TEST_DST "0,";
	    }
	}   
    }

    if(defined $opt_unigram) {
	if($train == 1) {
	    foreach (sort {$UnigramKey{$a}<=>$UnigramKey{$b}} keys %UnigramKey) {
		exists ${$UnigramHash{$id}}{$_} ? print TRAIN_DST "1," : print TRAIN_DST "0,";
	    }
	}
	else {
	    foreach (sort {$UnigramKey{$a}<=>$UnigramKey{$b}} keys %UnigramKey) {
		exists ${$UnigramHash{$id}}{$_} ? print TEST_DST "1," : print TEST_DST "0,";
	    }
	}
    }
    
    if(defined $opt_gender) {
	if($train == 1) { print TRAIN_DST "$genderHash{$id},"; }
	else { print TEST_DST "$genderHash{$id},"; }
    }
    

    my $tag = "";
    foreach (@{$icd9cmHash{$id}}) {
	$tag .= $_ . "_";
    }
    chop $tag;
    
    if($train == 1) {
	print TRAIN_DST "$tag\n";
    }
    else {
	print TEST_DST "$tag\n";
    } 
}

sub checkUnigram {
    if (defined $opt_unigram) {
	
	if(defined $opt_cutoff) { $cutoff = $opt_cutoff; }
	
	open(FILE, $opt_unigram) || die "Could not open co-occur file: $opt_unigram\n";
	while(<FILE>) {
	    chomp; $_= lc($_);
	    my @array = split/ : /;

	    $unigramHash{$array[0]}{$array[1]} = $array[2];
	} close FILE;
    }
}
sub checkOverlap {
    if(defined $opt_overlap) {
	
	open(FILE, $opt_overlap) || die "Could not open overlap file: $opt_overlap\n";
	
	while(<FILE>) {
	    chomp;
	    my ($code, $text) = split/ : /;
	    	    
	    my @array = split/\|/, $text;
	    foreach my $term (@array) {
		$term=~s/\s+/ /g;
		$term=~s/^\s+//g; 
		$term=~s/\s+$//g;
		$term = lc($term);
		$overlapHash{$term} = $code;
	    }
	}
    }

    my $temp =  keys %overlapHash;
}

sub getTrainFeatures {
    
    my $file = shift; 

    open(TRAIN, $file) || die "Could not open training file: $file\n";

    while(<TRAIN>) {
	chomp;

	#  get the id of the document
	if($_=~/\<doc id=\"(.*)\" type/) {
	    $id = $1; $idCounter++;
	}
	
	# get the ICD9CM code
	if($_=~/<code origin=\"CMC_MAJORITY\" type=\"ICD\-9\-CM\"\>(.*)\<\/code\>/) {
	    push @{$icd9cmHash{$id}}, $1; 
	}
	
	#  set the ICD9CM codes
	if($_=~/\<\/codes\>/) {
	    my $codes = "";
	    foreach (sort @{$icd9cmHash{$id}}) {
		$codes .= $_ . "_";
	    } chop $codes; 
	    if($codes eq "") { next; }
	    $icd9cmCodes{$codes}++;
	    $id_code = $codes;
	}
	
	# get the words from CLINICAL_HISTORY section
	# or get the words from IMPRESSION section
	if(
	   ($_=~/\<text origin\=\"CCHMC_RADIOLOGY\" type\=\"CLINICAL_HISTORY\"\>(.*)\<\/text\>/) 
	   ||  
	   ($_=~/\<text origin\=\"CCHMC_RADIOLOGY\" type\=\"IMPRESSION\"\>(.*)\<\/text\>/)
	   ){
	 
	    $trainSentenceCount++;

	    my $line = lc($1);
	    my @array = split/\s+/, $line; 
	 
	    push @trainSentence, $#array;
	    
	    my $impression = 0;
	    if($_=~/IMPRESSION/) { $impression = 1; }
   	    
	    #  get overlap
	    if(defined $opt_overlap) {
		if($impression == 1)   {; }
		else { getOverlap($line); }
	    }

	    
	    my $counter = 0;
	    my $female  = 0; 
	    my $male    = 0;

	    my $previousWord = "";

	    foreach (@array) {

		$_=~s/[\'\.\,\?]//g;
		if($_ eq "") { next; }
		
		$trainSentenceLength++;

		#  check gender
		if($_=~/^female$/) { $female = 1; }
		if($_=~/^male$/)   { $male = 1;   }
		
		#  check tags
		if(defined $opt_tags) {
		    if($_=~/after/)    { $tagAfter{$id}++;      }
		    if($_=~/history/)  { $tagHistory{$id}++;    }
		    if($_=~/prior/)    { $tagPrior{$id}++;      }
		    if($_=~/previous/) { $tagPrevious{$id}++;   }
		    if($_=~/s\/p/)     { $tagSP{$id}++;         }
		    
		    if($_=~/post/) {
			if($previousWord=~/status/) {
			    $tagStatusPost{$id}++; 
			} else { $tagPost{$id}++; }
		    }
		}
		
		#  get bag of words
		if(defined $opt_bow) {
		    getBagOfWords($id, $_);

		}

		# get co-occur
		if(defined $opt_unigram) {
		    getUnigram($id, $id_code, $_);
		}
		$counter++;
	    }
	    
	    if($female == 1 and $male == 0) {
		$genderHash{$id} = "female";
	    }
	    elsif($male == 1 and $female == 0) {
	    $genderHash{$id} = "male";
	}
	    elsif(! (exists $genderHash{$id}) ) {
		$genderHash{$id} = "none";
	    }
	}
    }
}

sub getTestFeatures {
    
    open(TEST, $opt_test) || die "Could not open test file: $testFile\n";

    my $testCodeCounter = 1;

    while(<TEST>) {
	chomp;

	#  get the id of the document
	if($_=~/\<doc id=\"(.*)\" type/) {
	    $id = $1; $idCounter++; $testCodes{$id}++;
	    
	    #  for the actual test data where the codes
	    #  are not known
	    if(defined $opt_nocodes) {
		push @{$icd9cmHash{$id}}, "?"; 
	    }
	}
	
	# for the test data where the codes are known
	# get the ICD9CM code
	if(! (defined $opt_nocodes) ) {
	    if($_=~/<code origin=\"CMC_MAJORITY\" type=\"ICD\-9\-CM\"\>(.*)\<\/code\>/) {
		push @{$icd9cmHash{$id}}, $1; 
	    }
	}
	
	#  set the ICD9CM codes
	if($_=~/\<\/codes\>/) {
	    my $codes = "";
	    foreach (sort @{$icd9cmHash{$id}}) {
		$codes .= $_ . "_";
	    } chop $codes; 
	if($codes eq "") { next; }
	    $icd9cmCodes{$codes}++;
	$id_code = $codes;
	}
		
	# get the words from CLINICAL_HISTORY section
	# or get the words from IMPRESSION section
	if(
	   ($_=~/\<text origin\=\"CCHMC_RADIOLOGY\" type\=\"CLINICAL_HISTORY\"\>(.*)\<\/text\>/) 
	   ||  
	   ($_=~/\<text origin\=\"CCHMC_RADIOLOGY\" type\=\"IMPRESSION\"\>(.*)\<\/text\>/)
	   ){
	    
	    $testSentenceCount++;

	    my $line = lc($1);
	    my @array = split/\s+/, $line; 
	    
	    push @testSentence, $#array;
	    
	    my $impression = 0;
	    if($_=~/IMPRESSION/) { $impression = 1; }
   	    
	    #  get overlap
	    if(defined $opt_overlap) {
		if($impression == 1)   {; }
		else { testOverlap($line); }
	    }
	    
	    my $counter = 0;
	    my $female  = 0; 
	    my $male    = 0;
	    foreach (@array) {

		$_=~s/[\'\.\,\?]//g;
		if($_ eq "") { next; }
		
		$testSentenceLength++;

		#  check gender
		if($_=~/^female$/) { $female = 1; }
		if($_=~/^male$/)   { $male = 1;   }
		
	
		#  check tags
		if(defined $opt_tags) {
		    if($_=~/after/)    { $tagAfter{$id}++;      }
		    if($_=~/history/)  { $tagHistory{$id}++;    }
		    if($_=~/prior/)    { $tagPrior{$id}++;      }
		    if($_=~/previous/) { $tagPrevious{$id}++;   }
		    if($_=~/s\/p/)     { $tagSP{$id}++;         }
		    
		    if($_=~/post/) {
			if($previousWord=~/status/) {
			    $tagStatusPost{$id}++; 
			} else { $tagPost{$id}++; }
		    }
		}
		
		#  get bag of words
		if(defined $opt_bow) {
		    testBagOfWords($id, $_);

		}

		# test co-occur
		if(defined $opt_unigram) {
		    testUnigram($id, $id_code, $_);
		}
		$counter++;
	    }
	    
	    if($female == 1 and $male == 0) {
		$genderHash{$id} = "female";
	    }
	    elsif($male == 1 and $female == 0) {
	    $genderHash{$id} = "male";
	}
	    elsif(! (exists $genderHash{$id}) ) {
		$genderHash{$id} = "none";
	    }
	}
    }
}

sub getOverlap {
    
    my $line = shift;
    
    $line=~s/[\'\.\,\?]//g;
    foreach my $text (sort keys %overlapHash) {

	my $code = $overlapHash{$text};
	$text=~s/ NOS//g;
	$text=~s/\, [A-Za-z\s]+//g; 
	$text=~s/\; [A-Za-z\s]+//g; 
	$text=~s/\([a-z]+\)//g;
	
	if($line=~/$text/) {
	    ${$OverlapHash{$id}}{$code}++; 
	    $OverlapKey{$code} = exists $OverlapKey{$code} ? 
		$OverlapKey{$code} : $OverlapCounter++;
	}
    }
}

sub testOverlap {
    
    my $line = shift;
    
    $line=~s/[\'\.\,\?]//g;
    foreach my $text (sort keys %overlapHash) {
	
	my $code = $overlapHash{$text};
	
	$text=~s/ NOS//g;
	$text=~s/\, [A-Za-z\s]+//g; 
	$text=~s/\; [A-Za-z\s]+//g; 
	$text=~s/\([a-z]+\)//g;
	
	if($line=~/$text/) {
	    if(exists $OverlapKey{$code}) {
		${$OverlapHash{$id}}{$code}++; 
	    }
	}
    }
}

sub getBagOfWords {
    my $id = shift;
    my $word = shift;
    ${$BagOfWordsHash{$id}}{$word}++; 
    $BagOfWordsKey{$word} = exists $BagOfWordsKey{$word} ? 
	$BagOfWordsKey{$word} : $BagOfWordsCounter++;
}

sub testBagOfWords {
    my $id = shift;
    my $word = shift;
    
    if(exists $BagOfWordsKey{$word}) {
	${$BagOfWordsHash{$id}}{$word}++; 
    }
}

sub getUnigram {
    my $id      = shift;
    my $id_code = shift;
    my $word    = shift;

    if($unigramHash{$id_code}{$word} >= $cutoff)  {     
	${$UnigramHash{$id}}{$word}++; 
	$UnigramKey{$word} = exists $UnigramKey{$word} ? 
	    $UnigramKey{$word} : $UnigramCounter++;
    }
}

sub testUnigram {
    my $id      = shift;
    my $id_code = shift;
    my $word    = shift;
    
    if(exists $UnigramKey{$word}) {
	if($unigramHash{$id_code}{$word} < $cutoff)  { next; }
	else {	${$UnigramHash{$id}}{$word}++; }
    }
}


##############################################################################
#  SUB FUNCTIONS
##############################################################################

#  function to output minimal usage notes
sub minimalUsageNotes {
    
    print STDERR "Usage: xml2arff.pl [OPTIONS] PREFIX\n";
    askHelp();
}

#  function to output help messages for this program
sub showHelp() {

    print "Usage: xml2arff.pl [OPTIONS] PREFIX\n\n";
    
    print "The program takes training and test data in the Medical Challenge \n";
    print "2007 xml format. It extract user defined features and outputs the \n";
    print "information in .arff format for the WEKA data mining package.\n\n";

    print "REQUIREMENTS: \n\n";
    
    print "--test FILE              The test file (xml format).\n\n";
    
    print "--train FILE             The training file (xml format).\n\n";

    print "OPTIONS:\n\n";
    
    print "--bow                    Bag of words\n";
	
    print "--unigrams FILE          Highly frequency words in the report.\n";
    print "                         These are precalculated using the\n";
    print "                         classify.pl program\n\n";
    
    print "--cutoff NUMBER          The frequency threshold for the unigram option.\n\n";

    print "--gender                 Whether the report is referring to a male or female.\n\n";
    
    print "--overlap                Whether the text descriptions of the ICD9CM codes \n";
    print "                         are in the text of the report. The descriptions are \n";
    print "                         in a user defined file.\n\n";


    print "--tags                   Whether the following words are in the report: after, \n";
    print "                         history, prior, previous, statust post, or s/p. \n\n";

    print "--nocodes                The test data does not have the associated ICD9CM codes.\n\n";
    print "--version                Prints the version number.\n\n";
 
    print "--help                   Prints this help message.\n\n";
}

#  function to output the version number
sub showVersion {
    print '$Id: xml2arff.pl,v 1.5 2007/05/16 16:31:09 btmcinnes Exp $';
    print "\nCopyright (c) 2007, Ted Pedersen, Bridget McInnes, \n";
    print "John Carlis and Serguei Pakhomov\n";
}

#  function to output "ask for help" message when user's goofed
sub askHelp {
    print STDERR "Type xml2arff.pl --help for help.\n";
}
    
