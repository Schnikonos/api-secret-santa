# Secret Santa App

## Description

### Alternative
- In `%USERPROFILE%\Documents` create the following folders:
  - _secretSanta_
  - _java_
- Download java17 in zip format : https://www.oracle.com/fr/java/technologies/downloads/#java17-windows
  - extract the folder `jdk-17.x.x` in it to `%USERPROFILE%\Documents\java`
  - rename it to `jdk-17`
- Download the jar in the releases and put it in `%USERPROFILE%\Documents\secretSanta`
- Create the file `%USERPROFILE%\Documents\secretSanta\SecretSanta.bat` and put the following code inside

```shell
@echo off
setlocal

rem ########### VARIABLES ############
set PATH_TO_JAR=%USERPROFILE%\Documents\secretSanta
set JAVA_HOME=%USERPROFILE%\Documents\Java\jdk-17
rem ########### VARIABLES END ########

set runJar=secret-santa.jar

rem Set tha path to local jdk installation
set PATH=%JAVA_HOME%\bin;%PATH%

rem Launch the jar
start "SecretSanta" cmd /c "java -jar %runJar% & pause"

endlocal
```

## Usage
Set the following properties (in application-perso.yml or in a starting command line)

For gmail, the password can be gotten using the following instructions: https://support.google.com/mail/answer/185833?hl=en

```yaml
spring:
  mail:
    username: your-email@gmail.com
    password: your-email-password
```

```shell
java -Dspring.mail.username=your-email@gmail.com -Dspring.mail.password="your-email-password" -jar theJarName.jar
```

Packaging
```shell
jpackage --input target --name SecretSanta --main-jar api-secret-santa-0.0.1-SNAPSHOT.jar --main-class com.santa.secret.ApiSecretSantaApplication --type exe
```