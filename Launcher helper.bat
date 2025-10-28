@echo off

rem Change to the project's root directory if needed.
rem For this script to work, you should run it from the root of your project
rem where the src folder is located.

rem Compile the Java source files.
echo Compiling Java files...
javac -d bin src/main/java/org/example/dndapp/*.java
if %errorlevel% neq 0 (
echo Compilation failed.
pause
exit /b %errorlevel%
)

rem Run the Launcher class.
echo Starting the application...
java -cp bin org.example.dndapp.Launcher

rem Keep the window open after the application exits to view any messages.
pause