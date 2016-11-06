#Instructions

This project is built with Gradle Wrapper. Gradle Wrapper is an executable file with versions for unix and windows
(gradlew or gradlew.bat) that will download the dependencies required.  This does require that JAVA_HOME be set on the
machine running the project.

To run the project run:

`./gradlew run` This will start a print server and output the log to the console

To run the test suite run:

`./gradlew test`

To build the project including static code analysis and all of the tests run:

`./gradlew build`

If you have problems with running the gradle wrapper remember to make the file executable and the run the correct command
for the operating system (./gradlew for unix and gradlew for windows). If all else fails, contact jenn.strater@gmail.com

If you'd prefer, you can also use the gradle file to import the project into your favorite IDE (IntelliJ, Eclipse, etc)
