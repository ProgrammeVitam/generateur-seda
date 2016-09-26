SCRIPT_DIR=$(dirname $0)
java -classpath "$SCRIPT_DIR/conf:$SCRIPT_DIR/lib/*" -Dvitam.conf.folder=/tmp -Dvitam.tmp.folder=/tmp -Dvitam.data.folder=/tmp -Dvitam.log.folder=/tmp fr.gouv.vitam.generator.scanner.main.SedaGenerator "$SCRIPT_DIR" $1
