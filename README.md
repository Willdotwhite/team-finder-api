# Team Finder API

Kotlin application used to drive the GMTK Jam Team Finder website!

A simple JSON REST API for what is basically a BREAD app.

## Getting Started

### Running it in an editor/IDE

#### IntelliJ (or similar like Android Studio)

- Open the `settings.gradle.kts` file, it will prompt you to open it as a project or a file, choose project.
- Find the main function and click on the play button next to it or open the gradle toolwindow and use `team-finder-api > app > Tasks > application > bootRun`
- When running using the main function a profile can be set by opening the run configuration and adding `-Dspring.profiles.active=[profile]` to the "VM Options" input

### Running from the command line

`gradlew.bat bootRun`

To run with the dev profile (or other `,` separated) add the `-Dspring.profiles.active=dev` command line argument.
This will launch Spring with the configured profiles which would run code associated with said profile.
Right now it is used for the TeamSeeder

### Profiles

Here are some of the active profiles that you may want to set:

- `dev` - sets up test data
- `h2` - uses an in-memory SQL database
- `mysql` - uses a MySQL database

### Troubleshooting

- If you should be tempted to use IntelliJ with WSL... well, you better know what you're doing. 

## API (Draft 1.0, very WIlP)

TODO: Convet to Swagger docs

This is just some rough notes somewhere easily visible, I'll update these shortly

```
/users         GET POST
      /:id     GET PUT DELETE

/teams         GET POST
      /:id     GET PUT DELETE

/login         GET POST (library integration)
```

### Users

```
GET /users

Params (query string):
roles (int)    Bitwise ID of selected roles
tools (int)    Bitwise ID of selected tools
focus (int)    Bitwise ID of selected focus
page  (int)    Pagination offset (default: 1)

-- 

POST /users

Params:
(TODO: same as data for Users table)

-- 

GET /users/:id

Params (path):
id    (int)    ID of user
```

### Teams

```
GET /teams

Params (query string):
roles (int)    Bitwise ID of selected roles
tools (int)    Bitwise ID of selected tools
focus (int)    Bitwise ID of selected focus
page  (int)    Pagination offset (default: 1)

-- 

POST /teams

Params:
(TODO: same as data for Teams table)

-- 

GET /teams/:id

Params (path):
id    (int)    ID of user
```



## DB Schema (Draft 1.0, very WIP)

TODO: Convert to schema docs

This is just some rough notes somewhere easily visible, I'll update these shortly

### Users

```
Users
   id             INT         NOT_NULL UNSIGNED AUTO_INCREMENT
   sub            VARCHAR(?)  NOT_NULL -- 'subject' ID from OAuth2, used for data change access
   discord_id     VARCHAR(?)  NOT_NULL
   display_name   VARCHAT(?)  NOT_NULL
   roles          BIGINT      -- If we go with bitwise operations, MySQL will cast to BIGINT at compare time;
                              -- are we willing to trade storage for runtime performance?
   tools          BIGINT      -- See above
   focus          ENUM/BIGINT -- Unsure of impl yet


   PRIMARY KEY (id)           -- Used for user profile queries (front end)
   UNIQUE  KEY (sub)          -- Used for change permissions (back end)
```

### Teams

```
Teams
   id             INT         NOT_NULL UNSIGNED AUTO_INCREMENT
   author         INT         NOT_NULL
   team_name      VARCHAR(?)  NOT_NULL
   roles          BIGINT      -- See the Users table
   tools          BIGINT      -- See the Users table
   focus          ENUM/BIGINT -- See the Users table


   PRIMARY KEY (id)              -- Used for user profile queries (front end)
   FOREIGN KEY (author) users.id -- Used for user profile queries (front end)
```


