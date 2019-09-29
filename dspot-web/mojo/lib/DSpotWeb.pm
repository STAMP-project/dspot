package DSpotWeb;
use Mojo::Base 'Mojolicious';
use Mojo::Log;
use Minion;
use POSIX;
use Data::Dumper;



# This method will run once at server start
sub startup {
  my $self = shift;

  # Load configuration from hash returned by config file
  my $config = $self->plugin('Config');

  # Log to specific dspot file.
  my $dlog = Mojo::Log->new(path => 'log/dspot.log');

  my $ltime = strftime "%Y-%m-%d %H:%M:%S", localtime time;
  $dlog->info("Application started at $ltime.");
  $self->config({'dlog' => \$dlog});
  
  # Set layout for pages.
  $self->defaults(layout => 'default');

  # Get working directory for projects.
  my $wdir = "projects/";
  if ( exists($config->{'work_dir'}) && -d $config->{'work_dir'} ) {
      $wdir = $config->{'work_dir'};
      print "* Using working directory from conf [$wdir].\n";
  } else {
      $self->config({'work_dir' => $wdir});
      print "* Using default workding directory [$wdir].\n";
  }
  
  # Get mvn command to use for execution
  my $cmd = $config->{'mvn_cmd'};
  print "* Using CMD [$cmd].\n";
  
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
  $self->minion->add_task(run_dspot => sub {
      my ($job, $url, $params) = @_;

      chdir($wdir);
      
      
      # Create a git clone
#      my @ret_git = `git clone $url $dir`;
      
      # Check that we can actually run dspot
      
      # Run dspot
      print Dumper( `pwd` );
      #my @ret = `$cmd`;
      
      say 'This is a background worker process.';
		    });


  # Router
  my $r = $self->routes;

  # Normal route to controller
  $r->get('/')->to( 'dspot#welcome' );

  # Route to list of repositories
  $r->get('/repos/')->to( 'dspot#repos' );
  
  $r->get('/admin/new/')->to( 'dspot#create' );
}

1;
