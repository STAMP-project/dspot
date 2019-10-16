package DSpotWeb;
use Mojo::Base 'Mojolicious';
#use Mojo::Log;
use Minion;
use POSIX;
use Data::Dumper;
use File::Spec;
use File::Path 'make_path';
use Archive::Zip qw( :ERROR_CODES :CONSTANTS );

use Mojo::JSON qw/decode_json encode_json/;


# This method will run once at server start
sub startup {
  my $self = shift;

  # Load configuration from hash returned by config file.
  my $config = $self->plugin('Config');

  # Mojolicious plugin RenderFile to serve static files.
  #$self->plugin('RenderFile');
  
  # Create timestamp to log starting time.
  my $ltime = strftime "%Y-%m-%d %H:%M:%S", localtime time;
  
  # Set layout for pages.
  $self->defaults(layout => 'default');

  # Get working directory for projects.
  my $workspace = './';
  my $ldir = 'projects/';
  my $wdir;
  if ( exists($config->{'workspace'}) ) {
      print "* Using workspace from conf [" . $config->{'workspace'} . "].\n";
      $workspace = $config->{'workspace'};
  } else {
      print "* Using default workspace [$workspace].\n";
  }
  
  $wdir = File::Spec->catdir( ($workspace, $ldir) );
  if ( -d $wdir ) {
      print "* Work dir [$wdir] exists.\n";
  } else {
      # Create dir hierarchy
      make_path($wdir);
      chmod 0755, $wdir;

      print "* Work dir [$wdir] created.\n";
  }
  
  $self->config({'work_dir' => $wdir});
  $self->config({'workspace' => $workspace});

  # Add another "public" directory
  my $static = Mojolicious::Static->new;
  my $paths = $static->paths;
  push @{$static->paths}, $workspace;
  
  # Get mvn command to use for execution
  my $mvn_home = $config->{'mvn_home'} or die "ERROR Cannot find mvn_home.\n";
  my $mvn_bin = File::Spec->catdir( ($mvn_home, 'bin') );
  print "* Using mvn home [$mvn_home].\n";
  my $mvn_cmd = $config->{'mvn_cmd'} or die "ERROR Cannot find mvn_cmd.\n";
  $mvn_cmd = File::Spec->catdir( ($mvn_bin, $mvn_cmd) );
  print "* Using mvn command [$mvn_cmd].\n";
  my $dspot_cmd = $config->{'dspot_cmd'} or die "ERROR Cannot find dspot_cmd.\n";
  $dspot_cmd = File::Spec->catdir( ($mvn_bin, $dspot_cmd) );
  print "* Using dspot cmd [$dspot_cmd].\n";
  my $dspot_cmd_ext = $config->{'dspot_cmd_ext'} or die "ERROR Cannot find dspot_cmd_ext.\n";
  $dspot_cmd_ext = File::Spec->catdir( ($mvn_bin, $dspot_cmd_ext) );
  print "* Using dspot cmd ext [$dspot_cmd_ext].\n\n";

  # Just check we have what we need..
  my $mvn_test = File::Spec->catfile( $mvn_bin, 'mvn' );
  my @mvn_tst = `${mvn_test} --version`;
  
  # Log to specific dspot file.
#  my $dlog = Mojo::Log->new(path => 'log/dspot.log');
#  $dlog->info("# Application started at $ltime.");


  # Create a bunch of useful helpers.
  
  # Create a helper to call dlog from anywhere.
#  $self->helper( dlog => sub {
#    my $c = shift;
#    my $msg = shift || "Default message log";
#    $dlog->info($msg);
#		 });

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
    my ($job, $id, $url, $params) = @_;
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
	my @ret_git = `cd ${pdir_src}; git pull | tee ../output/git_pull.log`;
	$ret->{'log'} = join("\n", @ret_git);
    } else {
	# Create dir hierarchy
	make_path($pdir_out);
     	chmod 0755, $pdir_out;
	
	# Clone the repo.
	my @ret_git = `cd $pdir; git clone $url src/ | tee output/git_clone.log`;
	$ret->{'log'} = join( "\n", @ret_git );
    }

    print "  END of task git_run.\n";
    
    $job->finish($ret);
      			   });
  
  # Task to execute Maven on project.
  $self->minion->add_task( run_mvn => sub {
    my ($job, $id, $url, $hash, $extended) = @_;
    my $ret = {};
    
    print "# Executing mvn for repo [$id].\n";
    print "  Command is [$mvn_cmd].\n";

    my $pdir_src = File::Spec->catdir( ($wdir, $id, 'src') );
      
    # Run mvn
    my @mvn_ret = `cd ${pdir_src}; ${mvn_cmd} | tee ../output/mvn_test.log`;
    $ret->{'log'} = join( "\n", @mvn_ret);
    
    # Mark the job as finished.
    $job->finish($ret);
			   });

  # Task to run dspot on project.
  $self->minion->add_task( run_dspot => sub {
    my ($job, $id, $url, $hash, $extended) = @_;
    my $ret = {};

    # Get command from config file.
    my $cmd = $config->{'dspot_cmd'};

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
    print "  Executing DSpot command: [$cmd].\n";
    print "    MVN_HOME: [" . ( $ENV{'MVN_HOME'} || '' ) . "].\n";
    print "    JAVA_HOME: [" . ( $ENV{'JAVA_HOME'} || '' ). "].\n";

    my @ret_mvn = `cd ${pdir_src}; mvn --version`;
    my @o = grep { $_ =~ m!Apache Maven! } @ret_mvn;
    chomp @o;
    print "    " . $o[0] . "\n";
    @o = grep { $_ =~ m!Maven home! } @ret_mvn;
    chomp @o;
    print "    " . $o[0] . "\n";
    @o = grep { $_ =~ m!Java version! } @ret_mvn;
    chomp @o;
    print "    " . $o[0] . "\n";
    @o = grep { $_ =~ m!Java home! } @ret_mvn;
    chomp @o;
    print "    " . $o[0] . "\n";

    my @ret_dspot = `cd ${pdir_src}; $cmd | tee ../output/dspot.log`;
    $ret->{'log'} = join( "\n", @ret_dspot);

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
    if (not exists($conf->{$id})) {
	$conf->{$id}{'git'} = $url;
	
	# Write projects information to file.
	my $conf_json = encode_json( $conf );
	open my $fh, '>:encoding(UTF-8)', $projects or return { "Could not find [$projects]." };
	print $fh $conf_json;
	close $fh;

	print "  Project added to conf.\n";
    } else {
	print "  Project already in conf, not modifying anything.\n";
    }

    # Mark the job as finished.
    $job->finish($ret);
			   });
  

  # Router
  my $r = $self->routes;

  # Normal route to controller
  $r->get('/')->to( 'dspot#welcome' );

  # Route to list of repositories
  $r->get('/repos/')->to( 'dspot#repos' );
  $r->get('/repo/#repo')->to( 'dspot#repo' );
  
  $r->get('/about')->to( 'dspot#about' );

  $r->get('/new/')->to( 'dspot#create' );
  $r->post('/new/')->to( 'dspot#create_post' );

  $r->get('/jobs')->to( 'dspot#jobs' );

}

1;
