# lab 7
## Members:
* Erik Christensen
* Jack Langston
* Margot Murvihill
* Matt Mazzagatte
* Jason Jen
## Runtime Instructions:
### shortcut
just run `./run.sh` in the current project directory
### what is actually going on
1. set the jdbc driver jar file to the `CLASSPATH` variable

		`export CLASSPATH=$CLASSPATH:mysql-connector-java-8.0.16.jar:.`

2. add the mysql database user, password, and server url to the corresponding environment variables

        `export HP_JDBC_URL=jdbc:mysql://mysql.jj-jj.org/labseven`

		`export HP_JDBC_USER=365lab7`

		`export HP_JDBC_PW=warthogs77`

3. remove any and all old/irrelevant java class files. we will make new ones

		`rm -rf *.class`

4. compile

		`javac InnReservations.java`

5. run

		`java InnReservations`

## Environment Variables
* `HP_JDBC_URL` for the database server url
* `HP_JDBC_USER` for the database server username
* `HP_JDBC_PW` for the database server password

## Database Information
* host: `mysql.jj-jj.org`
* database: `labseven`
* user: `365lab7`
* password: `warthogs77`

## known issues
n/a
