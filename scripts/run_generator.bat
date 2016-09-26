@echo off
start "Siegfried" "%~dp0\run_siegfried.bat"
java -classpath "%~dp0/conf;%~dp0/lib/*" -Dvitam.conf.folder=%TMP% -Dvitam.tmp.folder=%TMP% -Dvitam.data.folder=%TMP% -Dvitam.log.folder=%TMP% fr.gouv.vitam.generator.scanner.main.SedaGenerator %~dp0 %1
taskkill /F /IM sf.exe >NUL
pause
