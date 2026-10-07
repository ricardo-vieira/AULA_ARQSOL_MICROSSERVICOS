@echo off
if not defined JAVA_HOME (
    if exist "C:\Program Files\Java\jdk-25" (
        set JAVA_HOME=C:\Program Files\Java\jdk-25
    )
)

if exist "%~dp0..\tools\apache-maven-3.9.6\bin\mvn.cmd" (
    call "%~dp0..\tools\apache-maven-3.9.6\bin\mvn.cmd" %*
) else (
    call mvn %*
)
