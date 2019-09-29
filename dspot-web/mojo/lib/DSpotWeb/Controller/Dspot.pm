package DSpotWeb::Controller::Dspot;
use Mojo::Base 'Mojolicious::Controller';

# Default welcome page
sub welcome {
  my $self = shift;

  # Render template "dspot/welcome.html.ep"
  $self->render(msg => 'Welcome to the Mojolicious real-time web framework!');
}

# Create a new project
sub create {
  my $self = shift;
  my $url = "testurl"; #$self->stash('url');
  
  my $job = $self->minion->enqueue(run_dspot => [$url] => {delay => 0});

  
  # Render template "dspot/welcome.html.ep"
  $self->render(template => 'dspot/welcome',
		msg => 'Welcome to the Mojolicious real-time web framework!');
}

# List of repositories
sub repos {
  my $self = shift;

  # Load configuration from application config
  my $wdir = $self->app->config('work_dir');
  print "work dir is $wdir.\n";

  $self->app->dlog->info('this is dspot log test.');
  
  my @repos = grep { -d } glob( "$wdir/*" ); 
  $self->stash('repos' => \@repos);

  # Render template "dspot/repos.html.ep"
  $self->render();
}

# Display a specific repository
sub repo {
  my $self = shift;

  my $repo = $self->stash('repo');
	       
  # Load configuration from application config
  my $wdir = $self->app->config('work_dir');
  print "work dir is $wdir.\n";

  my @repos = grep { -d } glob( "$wdir/*" ); 
  $self->stash('repos' => \@repos);

  # Render template "dspot/repos.html.ep"
  $self->render();
}

1;
