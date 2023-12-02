# JiFra
Basic Java Project Manager CLI


## Commands
```js
jifra [command]
    init: initialize a new project (in current folder)
    run:  run the project compiles project and runs it
    clean: clean maded directories of tool (removes generated files)
    jar:   compiles && build a jar 
    war:   compiles && build a war
```

## Basic project structure
```sh
root
├── app.toml
├── Main.java
├── libs/
└── test-libs/
```

### Configuration of Project
- app.toml
```
name = "project"
group = "com.example"
version = "0.0.1"

[libs]
[test-libs]
```

### WebApps 
not fully implemented but slight support. Running war will then generated war file.
- needs file web.toml
```toml
# not fully supported
# [filter.SimpleFilter]
# defines class : urls-pattern
# class = "com.example.web.filter.SimpleFilter:/filter/me/*"
# param = "encoding:UTF-8,usefilter:true"

[servlet]
# subpackage : urls : on_load
# UserServlet = "user.UserServlet:/user:0"
# HomeServlet = "home.HomeServlet:/:/home"
```






