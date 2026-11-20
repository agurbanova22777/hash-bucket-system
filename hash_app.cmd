 @echo off
rem launcher for Windows: hash_app 5 20
rem it compiles HashApp.java first if there is no HashApp.class yet
if not exist "%~dp0HashApp.class" javac -d "%~dp0." "%~dp0HashApp.java"
java -cp "%~dp0." HashApp %*
