package DSpotWeb::Controller::Dspot;
use Mojo::Base 'Mojolicious::Controller';

use File::Spec;
use Data::Dumper;
use File::Basename;
use File::Spec;
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
  
  my $url = $self->param('gurl');
  my $hash = $self->param('hash');
  my $extended = $self->param('extended');

#  my $url = "https://github.com/STAMP-project/dspot.git"; #$self->stash('url');
  my @p = File::Spec->splitdir( $url );
  my ($repo, $org) = @p[-2..-1];
  $repo = defined($repo) ? $repo : 'Unknown Repo'; 
  $org = defined($org) ? $org : 'Unknown Org'; 
  my $id = "${repo}_${org}";
  print "Enqueue run_git $id $url $hash $extended.\n";  

  my $job_git = $self->minion->enqueue(
      run_git => [$id, $url, $hash] => {delay => 0});
  print "DBG JOB GIT " . Dumper($job_git);
  my $job_mvn = $self->minion->enqueue(
      run_mvn => [$id, $url, $hash, $extended] => {parents => [$job_git]});
  print "DBG JOB MVN " . Dumper($job_mvn);
  my $job_dspot = $self->minion->enqueue(
      run_dspot => [$id, $url, $hash, $extended] => {parents => [$job_mvn]});
  print "DBG JOB DSPOT " . Dumper($job_dspot);
  
  # Render template "dspot/create_post.html.ep"
  $self->redirect_to('/jobs');
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
  
  # Read projects information.
  my $contents = do {
      open my $fh, '<:encoding(UTF-8)', $projects or $msg = "Could not find [$projects]." ;
      local $/;
      <$fh>;
  };
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
  # Should we keep it?
  #my $pinfo = File::Spec->catfile( $wdir, $repo, 'project_info.json');
  
  # Read projects information.
  my $projects = File::Spec->catfile( $wdir, 'projects.json');
  print "Projects file is $projects.\n";
  my $contents = do {
      open my $fh, '<:encoding(UTF-8)', $projects or print "ERROR Could not find [$projects].\n" ;
      local $/;
      <$fh>;
  };
  print "JSON raw " . Dumper($contents);
  my $conf = decode_json( $contents );

  $self->stash('conf' => $conf->{$repo});
  $self->stash('repo' => $repo);
  $self->stash('wdir' => $wdir);
  
  # Render template "dspot/repo.html.ep"
  $self->render();
}

# Display the list of jobs
sub jobs {
  my $self = shift;

  my $msg;

  
  # Render template "dspot/jobs.html.ep"
  $self->render();
}




1;
