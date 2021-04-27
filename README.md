# Team Finder API

Kotlin application used to drive the GMTK Jam Team Finder website!

A simple JSON REST API for what is basically a BREAD app.


## API (Draft 1.0, very WIP)

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


