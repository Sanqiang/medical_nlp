#! /usr/bin/perl -ws

## Developed by the Computational Medicine Center for the 2007 International challenge
## classifying clinical free text using natural language processing. 
## 8 Feb 2007

use strict;
use XML::Simple;

my ( $xml, %id_ev, %id_gs, $id, $code, %fp, %fn, %tp, $fp, $tp, $fn  );

## get codes for both files
$xml = XMLin ( $ARGV[0], 'ForceArray'=>'1' );
foreach $id ( keys %{$xml->{'doc'}} ) {
  foreach ( @{$xml->{'doc'}{$id}{'codes'}[0]{'code'}} ) {
    $id_gs{$id}{$_->{'content'}}++;
  }
}
$xml = XMLin ( $ARGV[1], 'ForceArray'=>'1' );
foreach $id ( keys %{$xml->{'doc'}} ) {
  foreach ( @{$xml->{'doc'}{$id}{'codes'}[0]{'code'}} ) {
    $id_ev{$id}{$_->{'content'}}++;
  }
}

$tp = $fp = $fn = 0;
## balanced f-1 measure 
foreach $id ( keys %id_gs ) {
  %fp = %fn = %tp = ();
  foreach $code ( keys %{$id_gs{$id}} ) {
    if ( !defined $id_ev{$id}{$code} ) {
      $fn{$code}++
    } elsif ( defined $id_ev{$id}{$code} ) {
      $tp{$code}++;
    }
  }
  foreach $code ( keys %{$id_ev{$id}} ) {
    if ( !defined $id_gs{$id}{$code} ) {
      $fp{$code}++
    }
  }
  $tp += keys %tp;
  $fp += keys %fp;
  $fn += keys %fn;
}
## f = 2*p*r/(p+r)
$_ = (2*(( $tp/($tp+$fp) )*( $tp/($tp+$fn) )))/(( $tp/($tp+$fp) )+( $tp/($tp+$fn) ));
print "F-1 measure $_\n";
$_ = int(100*(sprintf("%.2f",$_)));
exit($_);

