CURRENTDIR=$(dirname $0)
mkdir $CURRENTDIR/build
mkdir $CURRENTDIR/build/conf
mkdir $CURRENTDIR/build/lib
cp $CURRENTDIR/run* $CURRENTDIR/build
cp $CURRENTDIR/metadata.json $CURRENTDIR/build
cp $CURRENTDIR/playbook_BinaryDataObject.json $CURRENTDIR/build/conf
cp $CURRENTDIR/logback.xml  $CURRENTDIR/build/conf
cp $CURRENTDIR/../scanner/target/*.jar $CURRENTDIR/build/lib
cp $CURRENTDIR/../scanner/target/dependencies/*.jar $CURRENTDIR/build/lib

