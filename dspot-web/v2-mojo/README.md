
# DSpot Web UI

## About

This directory contains everything needed to run a demonstration web server for DSpot.


## Installation

This application uses the [Mojolicious web framework](https://mojolicious.org/) and relies on a Postgres DB to store the task management system. Results are stored on disk.

You will need to install Perl and Mojolicious, plus a few other plugins. It is good practice to use [perlbrew](https://perlbrew.pl/) to manage one's perl installation, although it is not required. Then requirements can be installed with:

```
$ cpanm Mojolicious
$ cpanm Mojolicious::Plugin::Minion Mojolicious::Plugin::Minion::Admin Mojolicious::Plugin::Mail
$ cpanm Mojo::Pg
$ cpanm Archive::Zip File::Copy::Recursive DateTime
```

### Initialising the database

Setup a PostgreSQL DB for Minion, the job management framework:

```
postgres=# CREATE USER dspot_minion WITH PASSWORD 'pass4minion';
CREATE ROLE
postgres=# CREATE DATABASE dspot_minion OWNER dspot_minion;
CREATE DATABASE
```


## Configuration

Configuration is done in file `d_spot_web.conf`:

```
{
  'secrets' => ['abc7qzverq3rcqevq0d178qzabvftq96evfab563'],

  # See README.md for Posgres configuration. Only used for job management.
  'conf_pg_minion' => 'postgresql://dspot_pg_user:dspot_pg_password@/dspot_minion',

  # URL of the host (used for emails).
  'hostname' => 'https://dspot.myhost.com',

  # MAVEN_HOME.
  'mvn_home' => '~/apache-maven-3.6.0/',

  # Command to run when invoking mvn.
  'mvn_cmd' => 'mvn clean install -DskipTests',

  # Command to run when invoking dspot-maven.
  'dspot_cmd' => 'mvn eu.stamp-project:dspot-maven:3.1.0:amplify-unit-tests -Doutput-path=../output/dspot/',

  # Places where all repos will be cloned (and analysed).
  'workspace' => '/data/dspot_workspace/',
}
```

Parameters that can be adjusted are:

 * **secrets** is a customised string used to create safe cookies.
 * **conf_pg_minion** is the URL + credentials of the PostGreSQL cluster used by the job management component (Minion).
 * **hostname** is used to build the instance's URL in the emails sent.
 * **mvn_home** is the path to the maven installation that will be used for analysis.
 * **mvn_cmd** is the pre-analysis command to use in order to build the dependency tree.
 * **dspot_command** is the command to execute to run dspot (parameters for the mutation method are added dynamically).
 * **workspace** is the directory where the projects will be cloned and analysed. Please remember that cloned projects can be substancially big and used disk space might increase substancially.


## Run the application

Mojolicious runs [hypnotoad](https://mojolicious.org/perldoc/Mojo/Server/Hypnotoad) to serve the application. It can be safely used for production-grade setups.

```
$ hypnotoad script/dspot_web
```

The application is served on http://localhost:8080.

You can run the same command again for automatic hot deployment.

```
$ hypnotoad script/dspot_web
* Using workspace from conf [/data/dspot_workspace/].
* Using hostname from conf [http://ci4.castalia.camp:3000].
* Work dir [/data/dspot_workspace/projects] exists.
* Jobs dir [/data/dspot_workspace/jobs] exists.
* JSON projects file [/data/dspot_workspace/projects/projects.json] exists.
* Checking config..
  - Using mvn home [~/Applis/apache-maven-3.6.3/].
  - Using mvn command [MAVEN_HOME=~/Applis/apache-maven-3.6.3/ ~/Applis/apache-maven-3.6.3/bin/mvn clean install -DskipTests].
  - Using dspot cmd [MAVEN_HOME=~/Applis/apache-maven-3.6.3/ ~/Applis/apache-maven-3.6.3/bin/mvn eu.stamp-project:dspot-maven:3.1.0:amplify-unit-tests -Doutput-path=../output/dspot].

Starting hot deployment for Hypnotoad server 16493.
```

The server can be stopped using the `-s` flag:

```
$ hypnotoad -s script/dspot_web
* Using workspace from conf [/data/dspot_workspace/].
* Using hostname from conf [http://ci4.castalia.camp:3000].
* Work dir [/data/dspot_workspace/projects] exists.
* Jobs dir [/data/dspot_workspace/jobs] exists.
* JSON projects file [/data/dspot_workspace/projects/projects.json] exists.
* Checking config..
  - Using mvn home [~/Applis/apache-maven-3.6.3/].
  - Using mvn command [MAVEN_HOME=~/Applis/apache-maven-3.6.3/ ~/Applis/apache-maven-3.6.3/bin/mvn clean install -DskipTests].
  - Using dspot cmd [MAVEN_HOME=~/Applis/apache-maven-3.6.3/ ~/Applis/apache-maven-3.6.3/bin/mvn eu.stamp-project:dspot-maven:3.1.0:amplify-unit-tests -Doutput-path=../output/dspot].

Stopping Hypnotoad server 16571 gracefully.
```
