@echo off
setlocal

REM Helper script to run the app with the 'dev' profile on Windows.
REM Usage:
REM   run-dev.bat                -> runs on default port 8080
REM   run-dev.bat 8081           -> runs on port 8081

set PORT=%1

set MAVEN_CMD=mvnw.cmd
if not exist "%MAVEN_CMD%" (
  set MAVEN_CMD=mvn
)

if defined PORT (
  echo Starting with profile=dev on port %PORT% ...
  "%MAVEN_CMD%" spring-boot:run -Dspring-boot.run.profiles=dev -Dspring-boot.run.arguments="--server.port=%PORT%"
) else (
  echo Starting with profile=dev on default port 8080 ...
  "%MAVEN_CMD%" spring-boot:run -Dspring-boot.run.profiles=dev
)

endlocal
