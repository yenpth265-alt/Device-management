@echo off
chcp 65001 >nul
cd /d "%~dp0"

if not exist out mkdir out

dir /s /b "src\*.java" > sources.txt

javac -encoding UTF-8 -d out @sources.txt
if errorlevel 1 (
    echo Bien dich that bai.
    pause
    exit /b 1
)

java -Dfile.encoding=UTF-8 -cp out devicemanagement.Main %*
pause