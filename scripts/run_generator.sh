SCRIPT_DIR=$(dirname $0)
java -classpath "$SCRIPT_DIR/conf:$SCRIPT_DIR/lib/*" fr.gouv.vitam.generator.scanner.main.SedaGenerator "$SCRIPT_DIR" $1
