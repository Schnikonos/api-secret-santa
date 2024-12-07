# Secret Santa App

## Description


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