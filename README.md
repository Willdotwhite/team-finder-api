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
