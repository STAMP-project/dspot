package DSpotWebUI;
use Mojo::Base 'Mojolicious';
#use Mojo::Base 'Mojolicious::Plugin';
use Minion;
use Data::Dumper;

# This method will run once at server start
sub startup {
  my $self = shift;

  # Set layout for pages.
  $self->defaults(layout => 'default');

  # Load configuration from hash returned by "my_app.conf"
  my $config = $self->plugin('Config');
  
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
      my ($job, @args) = @_;

      # Check that we can actually run dspot

      # Run dspot
      my $cmd = "mvn eu.stamp-project:dspot-maven:amplify-unit-tests";
      print Dumper( `pwd` );
      #my @ret = `$cmd`;
      
      say 'This is a background worker process.';
		    });
  
  # Start a worker to perform up to 12 jobs concurrently
  my $worker = $self->minion->worker;
  $worker->status->{jobs} = 12;
  $worker->run;
  
  # Documentation browser under "/perldoc"
  $self->plugin('PODRenderer') if $config->{perldoc};

  # Router
  my $r = $self->routes;

  # Route to list of repositories
  $r->get('/repos/')->to( 'dspot#repos' );
  
  $r->get('/admin/create/#id')->to( 'dspot#create' );

  # Normal route to controller
  $r->get('/')->to( 'dspot#welcome' );
}

1;
