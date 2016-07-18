@echo off
start "Siegfried" "%~dp0\run_siegfried.bat"
java -classpath "%~dp0/conf;%~dp0/lib/*" fr.gouv.vitam.generator.scanner.main.SedaGenerator %~dp0 %1
taskkill /F /IM sf.exe >NUL
