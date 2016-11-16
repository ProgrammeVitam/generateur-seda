CURRENTDIR=$(dirname $0)
rm -Rf $CURRENTDIR/build
mkdir $CURRENTDIR/build
mkdir $CURRENTDIR/build/conf
mkdir $CURRENTDIR/build/conf/siegfried
mkdir $CURRENTDIR/build/lib
mkdir $CURRENTDIR/build/doc
cp $CURRENTDIR/run* $CURRENTDIR/build
cp $CURRENTDIR/sf.exe $CURRENTDIR/build
cp $CURRENTDIR/../Readme.rst $CURRENTDIR/build
cp $CURRENTDIR/../CHANGELOG $CURRENTDIR/build
cp $CURRENTDIR/ArchiveTransferConfig.json $CURRENTDIR/build/conf
cp $CURRENTDIR/playbook_BinaryDataObject.json $CURRENTDIR/build/conf
cp $CURRENTDIR/logback.xml  $CURRENTDIR/build/conf
cp $CURRENTDIR/default.sig  $CURRENTDIR/build/conf/siegfried/
cp $CURRENTDIR/../scanner/target/*.jar $CURRENTDIR/build/lib
cp $CURRENTDIR/../scanner/target/dependencies/*.jar $CURRENTDIR/build/lib
cp $CURRENTDIR/../doc/* $CURRENTDIR/build/doc
