@echo off
setlocal
if not exist out mkdir out
javac -d out src\main\java\com\example\paint\*.java
if errorlevel 1 (
    echo Compilation echouee.
    exit /b 1
)
java -cp out com.example.paint.PaintApp
