# Team Finder API

Kotlin application used to drive the GMTK Jam Team Finder website!

A simple JSON REST API for what is basically a BREAD app.

## Getting Started

This application uses `gradle` as a build tool. Your development process should let you update the codebase and re-run
gradle commands with relative ease, as you'll be doing this a lot.

Configuration files are called 'profiles' in this environment - they follow the format 
`app/src/main/resources/application-{profileName}.properties`. If you want to use the configuration in the 
`application-mysql.properties` file, you need to run the application with the `mysql` profile. 

### Running it in an editor/IDE

#### \[Recommended\] IntelliJ IDEA (or similar)

An IntelliJ IDE is recommended because of the maturity of their gradle/Kotlin integrations.

* Open the `settings.gradle.kts` file, it will prompt you to open it as a project or a file - choose `project`.
* Find the main function (`app/src/main/kotlin/team/finder/api/App.kt`) and either:
  * click on the green Play button to the left of `fun main()`
  * open the gradle tool window and use `team-finder-api > app > Tasks > application > bootRun`
* When running using the main function a profile can be set by opening the run configuration and adding `-Dspring.profiles.active=[profile]` to the "VM Options" input

You can set multiple profiles by separating each name with a comma.

#### Running from the command line

The gradle command to use is `bootRun`. Currently, `bootRun` doesn't handle command line arguments (in specific, 
`-Dspring.profiles.active=mysql,credentials`, or appropriate equivalent), so you need to build a JAR file and run that
with your intended config:

```bash
./gradlew build (or gradlew.bat if you\'re on Windows) 
java -jar ./app/build/libs/app.jar -Dspring.profiles.active=credentials,mysql
```

(To be confirmed: I can't get this working on my machine, apparent environmental issue.)

### Profiles

Here are some of the active profiles that you need to set for full functionality of the API:

* `credentials` (required)
  * points to `app/src/main/resources/application-credentials.properties`, a gitignored file for locally storing remote secrets
  * this file is required for discord OAuth2 integration


The API is a wrapper around a database, so you'll need some sort of database to support the API - pick one of:

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

### Endpoints

#### Teams

There are currently three Team-related endpoints exposed by the API:
  * /teams
    * GET: returns the list of non-deleted teams. Takes up to three optional parameters:
      * page: the page number to return out of the selected teams. Default is 1.
      * skillsetMask: a bit mask identifying which skills to filter by. Default is 0 (i.e. no filter). Valid range: 0 to ???.
      * order: whether to sort the selected teams in any way. Default is 'asc'. Valid range: 'asc' (ascending), 'desc' (descending) and 'random' (duh).
    * POST: creates a new team linked to the logged-in user (requires an authorization header). Takes one mandatory parameter:
      * teamDto: a TeamDto object containing the team's details. User's name and ID are automatically linked using the authorization details.
  * /teams/mine
    * GET: returns the logged-in user's team (if any). Accepts no parameters.
    * PUT: updates the logged-in user's team (if any). Requires an authorization header and takes one mandatory parameter:
      * teamDto: a TeamDto object containing the team's updated details.
    * DELETE: deletes the logged-in user's team (if any). Requires an authorization header.
  * /teams/report
    * POST: reports/flags a team. Requires an authorization header (no anonymous reporting) and takes one mandatory parameter:
      * teamId: the ID of the team to report.

#### Admin

There are currently five Admin-related endpoints exposed by the API (note that all of them check whether the user making the request is an admin):
  * /admin/reports
    * GET: returns a list of the teams with active reports.
  * /admin/banned-users
    * GET: returns a list of the users who have been marked as banned.
  * /admin/reports/clear
    * POST: resets a team's report count to 0. Takes one mandatory parameter:
        * teamId: the ID of the team to update.
  * /admin/delete-team
    * DELETE: marks a team as deleted. Takes one mandatory parameter:
      * teamId: the ID of the team to delete.
  * /admin/reinstate-team
    * POST: reinstates a team (i.e. marks it as no longer deleted). Takes one mandatory parameter:
      * teamId: the ID of the team to reinstate.
  * admin/ban-user
    * POST: marks a user as banned and their team as deleted (if any). Takes one mandatory parameter:
      * userId: the discord ID of the user to ban.
  * admin/redeem-user
    * POST: redeems a user (i.e. marks them as no longer banned). Takes one mandatory parameter:
        * userId: the discord ID of the user to redeem.

### Troubleshooting

- If you should be tempted to use IntelliJ with WSL... well, you better know what you're doing. 
