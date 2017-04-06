@echo off
java -classpath "%~dp0/conf;%~dp0/lib/*" -Dvitam.config.folder=%TMP% -Dvitam.tmp.folder=%TMP% -Dvitam.data.folder=%TMP% -Dvitam.log.folder=%TMP% fr.gouv.vitam.generator.csv.CSVGenerator %~dps0 %~fs1
pause
