package DSpotWeb;
use Mojo::Base 'Mojolicious';

use Minion;
use POSIX;
use Data::Dumper;

use File::Spec;
use File::Path 'make_path';
use File::Copy::Recursive qw(dircopy);

use Archive::Zip qw( :ERROR_CODES :CONSTANTS );

use Mojo::JSON qw/decode_json encode_json/;


# This method will run once at server start
sub startup {
  my $self = shift;

  # Load configuration from hash returned by config file.
  my $config = $self->plugin('Config');

  # Mojolicious plugin RenderFile to serve static files.
  my $conf_mail = {
    from     => 'STAMP team <stamp-demo@inria.fr>',
    type     => 'text/html',
  };
  $self->plugin('mail' => $conf_mail);

  # Create timestamp to log starting time.
  my $ltime = strftime "%Y-%m-%d %H:%M:%S", localtime time;
  
  # Set layout for pages.
  $self->defaults(layout => 'default');

  # Get working directory for projects.
  my $workspace = './';
  my $ldir = 'projects/';
  if ( exists($config->{'workspace'}) ) {
      print "* Using workspace from conf [" . $config->{'workspace'} . "].\n";
      $workspace = $config->{'workspace'};
  } else {
      print "* Using default workspace [$workspace].\n";
  }
  
  # Verification du répertoire de travail
  my $wdir = File::Spec->catdir( ($workspace, $ldir) );
  if ( -d $wdir ) {
      print "* Work dir [$wdir] exists.\n";
  } else {
      # Create dir hierarchy
      make_path($wdir);
      chmod 0755, $wdir;
      print "* Work dir [$wdir] created.\n";
  }

  # Verification du répertoire des jobs
  my $jobsdir = File::Spec->catdir( ($workspace, 'jobs') );
  if ( -d $jobsdir ) {
      print "* Jobs dir [$jobsdir] exists.\n";
  } else {
      # Create dir hierarchy
      make_path($jobsdir);
      chmod 0755, $jobsdir;
      print "* Jobs dir [$jobsdir] created.\n";
  }
  
  # Verification du fichier $wdir/projects.json
  my $wdirp = File::Spec->catfile( $wdir, 'projects.json' );
  if ( -e $wdirp ) {
      print "* JSON projects file [$wdirp] exists.\n";
  } else {
      my $d = {};
      my $json = encode_json($d);
      
      # Write projects information to file.
      open my $fh, '>:encoding(UTF-8)', $wdirp or return { "Could not find [$wdirp]." };
      print $fh $json;
      close $fh;
      
      print "* JSON projects file [$wdirp] created.\n";
  }
  
  $self->config({'work_dir' => $wdir});
  $self->config({'jobs_dir' => $jobsdir});
  $self->config({'workspace' => $workspace});

  # Add another "public" directory
  push @{$self->app->static->paths}, $workspace;
  
  # Get mvn command to use for execution
  my $mvn_home = $config->{'mvn_home'} or die "ERROR Cannot find mvn_home.\n";
  my $mvn_bin = File::Spec->catdir( ($mvn_home, 'bin') );
  print "* Checking config..\n";
  print "  - Using mvn home [$mvn_home].\n";

  my $mvn_cmd = $config->{'mvn_cmd'} or die "ERROR Cannot find mvn_cmd.\n";
  $mvn_cmd = "MAVEN_HOME=${mvn_home} " . File::Spec->catdir( ($mvn_bin, $mvn_cmd) );
  print "  - Using mvn command [$mvn_cmd].\n";

  my $dspot_cmd = $config->{'dspot_cmd'} or die "ERROR Cannot find dspot_cmd.\n";
  $dspot_cmd = "MAVEN_HOME=${mvn_home} " . File::Spec->catdir( ($mvn_bin, $dspot_cmd) );
  print "  - Using dspot cmd [$dspot_cmd].\n";

  # Just check we have what we need..
  my $mvn_test = File::Spec->catfile( $mvn_bin, 'mvn' );
  $mvn_test .= ' --version';
  my @mvn_test = `${mvn_test}`;


  # Create a help to get the path to wdir + project
  $self->helper( pdir => sub {
    my $c = shift;
    my $url = shift;

    my @p = File::Spec->splitdir( $url );
    my ($repo, $org) = @p[-2..-1];
    my $id = "${repo}_${org}";
    my $pdir = File::Spec->catdir( ($wdir, $id) );

    return $pdir;
		 });


  # Use Minion for job queuing.
  $self->plugin(
      'Minion' => {
	  Pg => $config->{'conf_pg_minion'}
      }
      );
  
  # Load plugin for Minion admin UI
  $self->plugin(
      'Minion::Admin' => {
	  return_to => '/',
	  route     => $self->app->routes->any('/admin/minion'),
      }
      );
  
  # Add tasks

  # Task to clone or pull the git repository.
  $self->minion->add_task( run_git => sub {
    my ($job, $id, $url, $hash) = @_;
    my $ret = {};

    print "# Executing git for [$id].\n";
    print "  URL is [$url].\n";
    
    my $pdir = File::Spec->catdir( ($wdir, $id) );
    my $pdir_out = File::Spec->catdir( ($pdir, 'output') );
    my $pdir_src = File::Spec->catdir( ($pdir, 'src') );
    print "  Working dir is [$pdir].\n";
    
    # If there is a directory already, then use it and pull.
    # Otherwise clone the repo.
    if ( -d $pdir ) {
	# Just make a pull.
	my @ret_git = `cd ${pdir_src}; git pull origin master | tee ../output/git_pull.log`;
	$ret->{'log'} = join("\n", @ret_git);
    } else {
	# Create dir hierarchy
	make_path($pdir_out);
     	chmod 0755, $pdir_out;
	
	# Clone the repo.
	my @ret_git = `cd $pdir; git clone $url src/`;
    }

    # Set the current commit to the specified tag/hash.
    if (defined($hash)) {
	# Checkout hash/tag.
	`cd ${pdir_src}; git checkout $hash | tee ../output/git_checkout.log`;
    }

    print "  END of task git_run.\n";
    
    $job->finish($ret);
      			   });
  
  # Task to execute Maven on project.
  $self->minion->add_task( run_mvn => sub {
    my ($job, $id, $url, $hash) = @_;
    my $ret = {};
    
    print "# Executing mvn for repo [$id].\n";
    print "  Command is [$mvn_cmd].\n";

    my $pdir_src = File::Spec->catdir( ($wdir, $id, 'src') );
      
    # Run mvn
    my @mvn_ret = `cd ${pdir_src}; ${mvn_cmd} | tee ../output/mvn_test.log`;
    $ret->{'log'} = join( "\n", @mvn_ret);
    
    print "  END of task run_mvn.\n";
    
    # Mark the job as finished.
    $job->finish($ret);
			   });

  # Task to run dspot on project.
  $self->minion->add_task( run_dspot => sub {
    my ($job, $id, $url, $hash, $email, $extended) = @_;
    my $ret = {};

    print "# Executing dspot for repo [$id].\n";
    print "  Command is [$dspot_cmd].\n";

    my $pdir = File::Spec->catdir( ($wdir, $id) );
    my $pdir_src = File::Spec->catdir( ($pdir, 'src') );
    my $pdir_out = File::Spec->catdir( ($pdir, 'output') );
    my $pdir_out_dspot = File::Spec->catdir( ($pdir, 'output', 'dspot') );

    # Create dir hierarchy
    make_path($pdir_out_dspot);
    chmod 0755, $pdir_out_dspot;

    # Check that we can actually run dspot
    
    # Run dspot
    print "  Executing DSpot command: [$dspot_cmd].\n";
    my @ret_mvn = `$mvn_test`;
    my @o = grep { $_ =~ m!Apache Maven! } @ret_mvn;
    chomp @o;
    print "    " . ($o[0] || 'Apache maven version not found') . "\n";
    @o = grep { $_ =~ m!Maven home! } @ret_mvn;
    chomp @o;
    print "    " . ($o[0] || 'Maven home not found') . "\n";
    @o = grep { $_ =~ m!Java version! } @ret_mvn;
    chomp @o;
    print "    " . ($o[0] || 'Java version not found') . "\n";
    @o = grep { $_ =~ m!Java home! } @ret_mvn;
    if (scalar(@o) != 0) { chomp @o };
    print "    " . ($o[0] || 'Java home not found') . "\n";
    print "EXT $extended.\n";
    my @ret_dspot;
    if ( $extended =~ m!^bconfig$! ) {
      $dspot_cmd = $dspot_cmd . ' -Diteration=1 -Damplifiers=FastLiteralAmplifier,MethodAdd,MethodRemove,MethodGeneratorAmplifier';
    } elsif ( $extended =~ m!^zconfig$! ) {
      $dspot_cmd = $dspot_cmd . ' -Diteration=1 -Damplifiers=MethodAdder,MethodRemove,FastAmpl,StringAmpl,ReturnValue,Nullifier';
    }

    print "  Executing [cd ${pdir_src}; $dspot_cmd | tee ../output/dspot.log]\n";
    @ret_dspot = `cd ${pdir_src}; $dspot_cmd | tee ../output/dspot.log`;
    $ret->{'log'} = join( "\n", @ret_dspot);
    
    my $d_out_dspot = File::Spec->catdir( ($wdir, $id, 'output', 'dspot') );
    my $results = [];
    if ( -d $d_out_dspot ) {
	my @files = <$d_out_dspot/*_report.json>;
	foreach my $f (@files) {
	    my $data;
	    {
		open my $fh, '<', $f or (print "Cannot find file $f." && next);
		$/ = undef;
		$data = <$fh>;
		close $fh;
	    }
	    my $conf = decode_json( $data );
	    push( @$results, $conf );
	}
    }

    my $nm = 0;
    map { $nm += $_->{'nbNewMutantKilled'} } @$results;
    $ret->{'totalNewMutantsKilled'} = $nm;

    # Copy files to workspace/jobs
    my $jobid = $job->{'id'};
    my $d_out_jobs = File::Spec->catdir( ($jobsdir, $jobid) );
    dircopy($d_out_dspot, $d_out_jobs);
    
    # Create a zip file including all results.
    print "  Zipping directory $pdir_out.\n";
    my $zip = Archive::Zip->new();
    # Add a directory
    my $dir_member = $zip->addTree( $pdir_out_dspot, 'output' );
    # Save the Zip file
    my $zip_file = File::Spec->catdir( ($pdir, 'results.zip') );
    unless ( $zip->writeToFileNamed( $zip_file ) == AZ_OK ) {
      die "ERROR zip write [$zip_file].";
    }
    
    # Read projects information.
    my $projects = File::Spec->catfile( $wdir, 'projects.json');
    my $data;
    {
	open my $fh, '<', $projects or die;
	$/ = undef;
	$data = <$fh>;
	close $fh;
    }
    my $conf = decode_json( $data );

    # Add project if it doesn't exist already.
#    if ( (not exists($conf->{$id})) or (not exists($conf->{$id}{'cmd'})) ) {
	$conf->{$id}{'git'} = $url;
	$conf->{$id}{'hash'} = $hash;
	$conf->{$id}{'cmd'} = $dspot_cmd;
 	
	# Write projects information to file.
	my $conf_json = encode_json( $conf );
	open my $fh, '>:encoding(UTF-8)', $projects or return { "Could not find [$projects]." };
	print $fh $conf_json;
	close $fh;

	print "  Project added/updated in conf.\n";
#    } else {
#	print "  Project already in conf, not modifying anything.\n";
#    }

    # Sending email.
    my $dspot_url = "ci4.castalia.camp:3000";
    my $maildata = "
<p>Hi,</p>

<p>Thank you for submitting your project to dspot-web. The job has been processed and the results can be found at [1].</p>

<p>[1] http://$dspot_url/job/$jobid</p>

<p>Have a wonderful day!</p>

--
the dspot-web bot

";
    eval {
      my $t = $self->mail(
	mail => {
          To => $email, 
          Format => 'mail',
          Subject => 'Your DSpot results are ready',
          Data => $maildata
        }
      );
    };
    if ($@) {
      print "WARNING: Could not send email to $email.";
      print $@;
    }

    print "  END of task run_dspot.\n";
    
    # Mark the job as finished.
    $job->finish($ret);
			   });
  

  # Router
  my $r = $self->routes;

  # Normal route to controller
  $r->get('/')->to( 'dspot#welcome' );

  # Route to list of repositories
  $r->get('/repo/#repo')->to( 'dspot#repo' );
  
  $r->get('/new/')->to( 'dspot#create' );
  $r->post('/new/')->to( 'dspot#create_post' );

  $r->get('/jobs')->to( 'dspot#jobs' );
  $r->get('/job/#job')->to( 'dspot#job' );

}

1;
