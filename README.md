# lab 7
## Members:
* Erik Christensen
* Jack Langston
* Margot Murvihill
* Matt Mazzagatte
* Jason Jen
## Runtime Instructions:
### shortcut:
just run `./run.sh` in the current project directory
### what actually is going on:
1. add the jdbc driver jar file to the `CLASSPATH` variable

`export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.`
2. add the mysql database user, password, and server url to the corresponding environment variables

`export HP_JDBC_URL=jdbc:mysql://mysql.jj-jj.org/labseven`

`export HP_JDBC_USER=365lab7`

`export HP_JDBC_PW=warthogs77`
