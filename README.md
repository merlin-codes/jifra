# JiFra
Basic Java Project Manager CLI


## Commands
```js
commands: 
	init [name]		- initialize project 
	clean			- clean project 
	install			- install all dependencies 
	run			- run program 
	help			- this help 
	jar			- make jar 
	war			- make war

chaining commands: 
	[command] -<flags>
	n - clean (new)
	i - install
	c - compile
	r - run
	j - make jar
	w - make war
	e - make .env
	s - save jar
```

## Basic project structure
```sh
root
├── app.toml
├── Main.java
├── libs/
├── local-libs/
└── test-libs/
```

### Configuration of Project
- app.toml
```toml
name = "Project"
group = "com.example.project"
version = "0.0.1"

[libs]
# artifact = "groupId:version" or "groupId"

[local]
# name-jar = /path/to/name.jar

[test-libs]
# artifact = "groupId:version" or "groupId"
```

### WebApps 
not fully implemented but slight support. Running war will then generated war file.
- require file web.toml
- used file can be used with TomCat10 (JakartaEE) 
```toml
# not fully supported
# [filter.SimpleFilter]
# defines class : urls-pattern
# class = "com.example.web.filter.SimpleFilter:/filter/me/*"
# param = "encoding:UTF-8,usefilter:true"

[servlet]
# subpackage : urls : on_load
# 0 is false
UserServlet = "user.UserServlet:/user:0"
HomeServlet = "home.HomeServlet:/:/home"
```






