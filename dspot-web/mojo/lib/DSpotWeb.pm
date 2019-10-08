package DSpotWeb;
use Mojo::Base 'Mojolicious';
#use Mojo::Log;
use Minion;
use POSIX;
use Data::Dumper;
use File::Spec;
use File::Path 'make_path';


use Mojo::JSON qw/decode_json encode_json/;


# This method will run once at server start
sub startup {
  my $self = shift;

  # Load configuration from hash returned by config file
  my $config = $self->plugin('Config');

  # Create timestamp to log starting time.
  my $ltime = strftime "%Y-%m-%d %H:%M:%S", localtime time;
  
  # Set layout for pages.
  $self->defaults(layout => 'default');

  # Get working directory for projects.
  my $wdir = "projects/";
  if ( exists($config->{'work_dir'}) && -d $config->{'work_dir'} ) {
      $wdir = $config->{'work_dir'};
      print "* Using working directory from conf [$wdir].\n";
  } else {
      $self->config({'work_dir' => $wdir});
      print "* Using default working directory [$wdir].\n";
  }
  
  # Get mvn command to use for execution
  my $cmd = $config->{'mvn_cmd'};
  print "* Using CMD [$cmd].\n";
  
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
    my $pdir = catdir($wdir, $id);

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

    print "Executing git for url [$url].\n";
    print "  ID is [$id].\n";
    
    my $pdir = File::Spec->catdir( ($wdir, $id) );
    my $pdir_out = File::Spec->catdir( ($pdir, 'output') );
    my $pdir_src = File::Spec->catdir( ($pdir, 'src') );
    print "  Working dir is [$pdir].\n";
    
    # If there is a directory already, then use it and pull.
    # Otherwise clone the repo.
    if ( -d $pdir ) {
	# Just make a pull.
	my @ret_git = `cd ${pdir_src}; git pull | tee ../output/git_pull.log`;
	$ret->{'git_log'} = join("\n", @ret_git);
	print Dumper(@ret_git);
    } else {
	# Create dir hierarchy
	make_path($pdir_out);
     	chmod 0755, $pdir_out;
	
	# Clone the repo.
	my @ret_git = `cd $pdir; git clone $url src/ | tee ../output/git_clone.log`; 
	print Dumper(@ret_git);
    }

    print "END of task git_run.\n";
    
    $job->finish($ret);
      			   });
  
  $self->minion->add_task( run_mvn => sub {
    my ($job, $id, $url, $hash, $extended) = @_;
    my $ret = {};
    
    print "Executing mvn for repo [$id].\n";

    my $pdir = File::Spec->catdir( ($wdir, $id) );
    my $pdir_src = File::Spec->catdir( ($pdir, 'src') );
    print "In task mvn 2 : $pdir, $pdir_src.\n";
    $ENV{"MAVEN_HOME"} = "/home/boris/Applis/apache-maven-3.6.0/";
    print Dumper( `echo "test \$MAVEN_HOME."` );
    chdir($pdir_src);
    print "In task mvn 3 after chdir.\n";
      
    # Run mvn
    my @mvn_ret = `cd ${pdir_src}; mvn clean test -DskipTests | tee ../output/mvn_test.log`;
    #print Dumper( @ );
    $ret->{'log'} = join( "\n", @mvn_ret);
    print "In task mvn 4 after pwd.\n";
    #my @ret = `$cmd`;
    
    # Mark the job as finished.
    $job->finish($ret);
			   });
  
  $self->minion->add_task( run_dspot => sub {
    my ($job, $id, $url, $hash, $extended) = @_;
    my $ret = {};
    
    print "Executing dspot for repo [$id].\n";

    my $pdir = File::Spec->catdir( ($wdir, $id) );
    my $pdir_src = File::Spec->catdir( ($pdir, 'src') );
    print "In task dspot 2 : $pdir, $pdir_src.\n";
    $ENV{"MAVEN_HOME"} = "/home/boris/Applis/apache-maven-3.6.0/";
    print Dumper( `echo "test \$MAVEN_HOME."` );
    chdir($pdir_src);
    print "In task dspot 3 after chdir.\n";
      
    # Check that we can actually run dspot
    
    # Run dspot
    my $cmd = $config->{'mvn_cmd'};
    print "  Executing DSpot command: [$cmd].\n";
    print "    MVN_HOME: [" . $ENV{'MVN_HOME'} . "].\n";
    print "    JAVA_HOME: [" . $ENV{'JAVA_HOME'} . "].\n";

    my @ret_mvn = `cd ${pdir_src}; mvn --version`;
    my @o = grep { $_ =~ m!Apache Maven! } @ret_mvn;
    print "    " . $o[0] . "\n";
    @o = grep { $_ =~ m!Maven home! } @ret_mvn;
    print "    " . $o[0] . "\n";
    @o = grep { $_ =~ m!Java version! } @ret_mvn;
    print "    " . $o[0] . "\n";
    @o = grep { $_ =~ m!Java home! } @ret_mvn;
    print "    " . $o[0] . "\n";

    my @ret_dspot = `cd ${pdir_src}; $cmd | tee ../output/dspot.log`;
    print Dumper( @ret_dspot );
    $ret->{'log'} = join( "\n", @ret_dspot);
    
    say 'Add this job to the list of repositories in projects.json.';

    # Read projects information.
    my $projects = File::Spec->catfile( $wdir, 'projects.json');
    my $data;
    {
	open my $fh, '<', $projects or die;
	$/ = undef;
	$data = <$fh>;
	close $fh;
    }
    print "DBG raw " . Dumper($data);
    my $conf = decode_json( $data );
    print "DBG json-decoded " . Dumper($conf);

    # Add project if it doesn't exist already.
    if (not exists($conf->{$id})) {
	$conf->{$id}{'git'} = $url;
	
	# Write projects information to file.
	my $conf_json = encode_json( $conf );
	open my $fh, '>:encoding(UTF-8)', $projects or return { "Could not find [$projects]." };
	print $fh $conf_json;
	close $fh;

	print "Project added to conf.\n";
    } else {
	print "Project already in conf, not modifying anything.\n";
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
