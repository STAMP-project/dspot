package DSpotWebUI::Controller::Dspot;
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

  
  
  # Render template "dspot/welcome.html.ep"
  $self->render(template => 'dspot/welcome',
		msg => 'Welcome to the Mojolicious real-time web framework!');
}

# List of repositories
sub repos {
  my $self = shift;

  # Render template "dspot/repos.html.ep"
  $self->render();
}

1;
