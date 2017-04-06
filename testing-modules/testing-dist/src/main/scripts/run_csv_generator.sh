#!/usr/bin/env sh
SCRIPT_DIR=$(dirname $0)
java -classpath "$SCRIPT_DIR/conf:$SCRIPT_DIR/lib/*" -Dvitam.conf.folder=/tmp -Dvitam.tmp.folder=/tmp -Dvitam.data.folder=/tmp -Dvitam.log.folder=/tmp fr.gouv.vitam.generator.csv.CSVGenerator "$SCRIPT_DIR" $1
