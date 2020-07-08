# CREATE DATABASE AND USER  

```
db = db.getSiblingDB('spring-demo')

db.createUser(
  {
    user: "spring-demo",
    pwd: "spring-demo",
    roles:
    [
      {
        role: "userAdminAnyDatabase",
        db: "admin"
      }
    ]
  }
)
```