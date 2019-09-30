package DSpotWeb::Controller::Dspot;
use Mojo::Base 'Mojolicious::Controller';

use File::Spec;
use Data::Dumper;
use File::Basename;
use Mojo::JSON qw/decode_json/;


# Welcome page
sub welcome {
  my $self = shift;

  # Render template "dspot/welcome.html.ep"
  $self->render(msg => 'Welcome to the Mojolicious real-time web framework!');
}

# About page
sub about {
  my $self = shift;

  # Render template "dspot/about.html.ep"
  $self->render();
}

# Create a new project -- GET
sub create {
  my $self = shift;
  
  # Render template "dspot/create.html.ep"
  $self->render();
}

# Create a new project -- POST
sub create_post {
  my $self = shift;
  
  my $url = "https://github.com/STAMP-project/dspot.git"; #$self->stash('url');
  print "Enqueue run_git.\n";

  #File::Spec->splitdir( $path );
  print "splitdir " . Dumper( File::Spec->splitdir( $url ) );

  
  my $job = $self->minion->enqueue(run_git => [$url] => {delay => 0});
  print "DBG JOB " . Dumper($job);
  
  # Render template "dspot/create_post.html.ep"
  $self->redirect_to('/');
}

# List of repositories
sub repos {
  my $self = shift;

  print "DBG \n";
  my $msg;
  
  # Load configuration from application config
  my $wdir = $self->app->config('work_dir');
  print "work dir is $wdir.\n";

#  $self->app->dlog('message log from controller.');
  
#  my @repos_ = grep { -d } glob( "$wdir/*" );
#  File::Spec->splitdir( $url )

  my $projects = File::Spec->catfile( $wdir, 'projects.json');
  
  print "DBG before josn \n";
  # Read projects information.
  my $contents = do {
      open my $fh, '<:encoding(UTF-8)', $projects or $msg = "Could not find [$projects]." ;
      local $/;
      <$fh>;
  };
  print "DBG " . Dumper($contents);
  my $conf = decode_json( $contents );

  print "DBG \n";

  
  my @repos = map { basename($_) } sort keys %{$conf};

  $self->stash('repos' => \@repos);

  # Render template "dspot/repos.html.ep"
  $self->render();
}

# Display a specific repository
sub repo {
  my $self = shift;

  my $msg;
  my $repo = $self->stash('repo');
	       
  # Load configuration from application config
  my $wdir = $self->app->config('work_dir');
  print "work dir is $wdir.\n";
  my $pinfo = File::Spec->catfile( $wdir, $repo, 'project_info.json');
  
  # Read projects information.
  my $contents = do {
      open my $fh, '<:encoding(UTF-8)', $pinfo or $msg = "Could not find [$pinfo]." ;
      local $/;
      <$fh>;
  };
  my $conf = decode_json( $contents );

  $self->stash('conf' => $conf);
  
  # Render template "dspot/repo.html.ep"
  $self->render();
}

1;
