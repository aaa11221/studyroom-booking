@echo off
setlocal

set WRAPPER_JAR=%~dp0.mvn\wrapper\maven-wrapper.jar

if not exist "%WRAPPER_JAR%" (
  echo Maven wrapper jar not found: %WRAPPER_JAR%
  exit /b 1
)

java -Dmaven.multiModuleProjectDirectory=%~dp0. -cp "%WRAPPER_JAR%" org.apache.maven.wrapper.MavenWrapperMain %*
exit /b %ERRORLEVEL%
