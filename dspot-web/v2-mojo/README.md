
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

### Initialising the application

Setup a PostgreSQL DB for Minion:

```
postgres=# CREATE USER dspot_minion WITH PASSWORD 'pass4minion';
CREATE ROLE
postgres=# CREATE DATABASE dspot_minion OWNER dspot_minion;
CREATE DATABASE
```


## Run the application

```
$ hypnotoad script/dspot_web
```

The application is served on http://localhost:3000.
