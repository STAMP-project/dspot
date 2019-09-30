package DSpotWeb;
use Mojo::Base 'Mojolicious';
#use Mojo::Log;
use Minion;
use POSIX;
use Data::Dumper;
use File::Spec;


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
    my ($job, $url, $params) = @_;

    my @p = File::Spec->splitdir( $url );
    my ($repo, $org) = @p[-2..-1];
    my $id = "${repo}_${org}";
    my $pdir = catdir($wdir, $id);
    my $pdir_out = catdir($pdir, 'output');
    my $pdir_src = catdir($pdir, 'src');
    
    
    # If there is a directory already, then use it and pull.
    # Otherwise clone the repo.
    if ( -d $pdir ) {

      # Go to project directory
      chdir($pdir_src);

      # Just make a pull.
      my @ret_git = `echo git pull`; 

    } else {

      # Create dir hierarchy
      mkdir($pdir, $pdir_out);
      	
      # Clone the repo.
      my @ret_git = `echo git clone $url src`; 

    }
      			   });
  
  $self->minion->add_task( run_dspot => sub {
    my ($job, $url, $params) = @_;
      
    my @p = File::Spec->splitdir( $url );
    my ($repo, $org) = @p[-2..-1];
    my $id = "${repo}_${org}";
    my $pdir = catdir($wdir, $id);
    my $pdir_src = catdir($pdir, 'src');

    chdir($pdir_src);
      
    # Check that we can actually run dspot
    
    # Run dspot
    print Dumper( `pwd` );
    #my @ret = `$cmd`;
    
    say 'This is a background worker process dspot.';
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
}

1;
