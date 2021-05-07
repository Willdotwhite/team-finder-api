# Team Finder API

Kotlin application used to drive the GMTK Jam Team Finder website!

A simple JSON REST API for what is basically a BREAD app.

## Getting Started

### Running it in an editor/IDE

#### IntelliJ (or similar like Android Studio)

- Open the `settings.gradle.kts` file, it will prompt you to open it as a project or a file, choose project.
- Find the main function and click on the play button next to it or open the gradle toolwindow and use `team-finder-api > app > Tasks > application > bootRun`
- When running using the main function a profile can be set by opening the run configuration and adding `-Dspring.profiles.active=[profile]` to the "VM Options" input

#### Running from the command line

`gradlew.bat bootRun`

To run with the dev profile (or other `,` separated) add the `-Dspring.profiles.active=dev` command line argument.
This will launch Spring with the configured profiles which would run code associated with said profile.
Right now it is used for the TeamSeeder

### Profiles

Here are some of the active profiles that you need to set for full functionality of the API:

#### 
* `credentials`
  * points to `application-credentials.properties`, a gitignored file for locally storing remote secrets
  * this file is required for discord OAuth2 integration

#### Database

The API is a wrapper around a database, so you'll need some sort of database to support the API:

* `h2` - uses an in-memory SQL database; easy to set up and use
* `mysql` - uses a MySQL database, which matches the remote architecture

### Database

#### Setting up H2

TODO: This please

#### Setting up MySQL

This example uses Docker, because setting up MySQL natively is a pain.

1. Create your MySQL container: `docker run --name mysql -p 3306:3306 -e MYSQL_ROOT_PASSWORD=password -d mysql:latest`
  * We forward the 3306 port in order to allow the app (which does _not_ run in Docker) to hit the DB directly
  * The `MYSQL_ROOT_PASSWORD` should match whatever is in `application-mysql.properties`
  * The default username is `root`, although this can be changed if necessary
1. Jump onto the Docker container: `docker exec -it mysql bash`
1. Connect to MySQL: `mysql -h localhost -u root -p`
   * The password is whatever you set `MYSQL_ROOT_PASSWORD` to be - default is `password`
1. Initialise the `jam` database: `CREATE DATABASE jam;`

You don't need to create the `teams` table in the DB for some reason - Java/SB magic allows the data to persist somehow.

### Troubleshooting

- If you should be tempted to use IntelliJ with WSL... well, you better know what you're doing. 
