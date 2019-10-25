package DSpotWeb::Controller::Dspot;
use Mojo::Base 'Mojolicious::Controller';

use File::Spec;
use File::Find;
use Data::Dumper;
use File::Basename;
use Mojo::JSON qw/decode_json/;
use Archive::Zip qw( :ERROR_CODES :CONSTANTS );


# Welcome page
sub welcome {
  my $self = shift;

  # Load configuration from application config
  my $wdir = $self->app->config('work_dir');
  my $projects = File::Spec->catfile( $wdir, 'projects.json');
  
  # Read projects information.
  my $contents = do {
      open my $fh, '<:encoding(UTF-8)', $projects or return "Could not find [$projects]." ;
      local $/;
      <$fh>;
  };
  my $conf = decode_json( $contents );
  
  my @repos = map { basename($_) } sort keys %{$conf};

  $self->stash('repos' => \@repos);

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
  my $email = $self->param('email');
  my $extended = $self->param('optionsRadios');

  my @p = File::Spec->splitdir( $url );
  my ($repo, $org) = @p[-2..-1];
  my $msg;

  # Validation

  # Git URL check.
  if ( (not defined($repo)) || (not defined($org)) ) {
    # Render template "dspot/create_post.html.ep"
    $self->flash(msg_nok => "Cannot recognise Git URL. Please try again.");
    $self->redirect_to('/new');
    return;
  }

  # Email check.
  if ($email !~ m!^.*\@.*$!) {
    $self->flash(msg_nok => "We need a valid email address to notify you when results are available.");
    $self->redirect_to('/new');
    return;
  }

  my $id = "${repo}_${org}";  
  print "# Start tasks for project $id.\n";  
  my $job_git = $self->minion->enqueue(
      run_git => [$id, $url, $hash] => {delay => 0});
  #  print "DBG JOB GIT " . Dumper($job_git);
  my $job_mvn = $self->minion->enqueue(
      run_mvn => [$id, $url, $hash] => {parents => [$job_git]});
  #  print "DBG JOB MVN " . Dumper($job_mvn);
  my $job_dspot = $self->minion->enqueue(
      run_dspot => [$id, $url, $hash, $email, $extended] => {parents => [$job_mvn]});
  #  print "DBG JOB DSPOT " . Dumper($job_dspot);
  
  $self->flash(msg_ok => "Jobs have been created.");
  $self->redirect_to('/jobs'); 
}


# Display a specific repository
sub repo {
  my $self = shift;

  my $msg;
  my $repo = $self->stash('repo');
	       
  # Load configuration from application config
  my $wdir = $self->app->config('work_dir');
  
  # Read projects information.
  my $projects = File::Spec->catfile( $wdir, 'projects.json');
  my $contents = do {
      open my $fh, '<:encoding(UTF-8)', $projects or print "ERROR Could not find [$projects].\n" ;
      local $/;
      <$fh>;
  };

  my $conf = decode_json( $contents );

  # Prepare data to be sent to template.
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


# Display a specific job
sub job {
  my $self = shift;

  my $msg;
  my $id = $self->stash('job');
	       
  # Load configuration from application config
  my $jobs_dir = $self->app->config('jobs_dir');
  my $jdir = File::Spec->catdir( ($jobs_dir, $id) );
  my $wdir = $self->app->config('work_dir');
  
  # Read projects information.
  my $projects = File::Spec->catfile( $wdir, 'projects.json');
  my $contents = do {
      open my $fh, '<:encoding(UTF-8)', $projects or print "ERROR Could not find [$projects].\n" ;
      local $/;
      <$fh>;
  };
  my $conf = decode_json( $contents );
  
  my %jobs = %{$self->app->minion->backend->list_jobs(0, 1, { ids => [$id] })};
  my $myjob = $jobs{'jobs'}[0];

  my @tests = ();
  if (-d $jdir) {
      find( sub { $_ =~ m/.*\.java/ && push(@tests, $File::Find::name) }, $jdir );
  }

  # Prepare data to be sent to template.
  $self->stash('conf' => $conf->{$myjob->{'args'}[0]});
  $self->stash('jdir' => $jdir);
  $self->stash('myjob' => $myjob);
  $self->stash('tests' => \@tests);
  
  # Render template "dspot/job.html.ep"
  $self->render();
}


# # Download specific files from wdir.
# sub dl {
#   my $self = shift;

#   my $lfile = $self->stash('file');

#   my $wdir = $self->app->config('work_dir');
#   my $file = File::Spec->catdir( ($wdir, $lfile) );
#   print "  Serving static file [$file].\n";
#   if ( -f $file ) {
#       # Render static file from work_dir.
#       $self->reply->file($file);
#   } else {
#       $self->reply->not_found;
#   }
# }

1;
