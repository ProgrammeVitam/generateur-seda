CURRENTDIR=$(dirname $0)
rm -Rf $CURRENTDIR/build
mkdir $CURRENTDIR/build
mkdir $CURRENTDIR/build/conf
mkdir $CURRENTDIR/build/conf/siegfried
mkdir $CURRENTDIR/build/lib
cp $CURRENTDIR/run* $CURRENTDIR/build
cp $CURRENTDIR/metadata.json $CURRENTDIR/build/conf
cp $CURRENTDIR/playbook_BinaryDataObject.json $CURRENTDIR/build/conf
cp $CURRENTDIR/logback.xml  $CURRENTDIR/build/conf
cp $CURRENTDIR/default.sig  $CURRENTDIR/build/conf/siegfried/
cp $CURRENTDIR/../scanner/target/*.jar $CURRENTDIR/build/lib
cp $CURRENTDIR/../scanner/target/dependencies/*.jar $CURRENTDIR/build/lib
touch $CURRENTDIR/build/conf/generator.properties
