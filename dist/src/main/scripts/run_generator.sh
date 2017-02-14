#!/bin/sh
SCRIPT_DIR=$(dirname $0)
if [ "x$1" != "x" ];then 
    TARGET_DIR=$(realpath "$1")
fi
java -classpath "$SCRIPT_DIR/conf:$SCRIPT_DIR/lib/*" -Dvitam.config.folder=/tmp -Dvitam.tmp.folder=/tmp -Dvitam.data.folder=/tmp -Dvitam.log.folder=/tmp fr.gouv.vitam.generator.scanner.main.SedaGenerator "$SCRIPT_DIR" "$TARGET_DIR"
