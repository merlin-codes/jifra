# java -jar ../jifra-cli/jiFraWork.jar jar
# jifra jar
# java -jar  ./JiFraCLI.jar jar

# java -jar ./JiFraCLI.jar run
# exit(0)






# ORIGINAL usage without program build already
src=./src.oldways
target=./target.oldways
group=dev/levia/jifra
group_java=$(echo "$group" | sed -e 's/\//\./g')
sources=sources.txt

echo $group_java

rm -rf $src
rm -rf $target

mkdir -p $src/$group
mkdir -p $target/$group
mkdir -p $target/META-INF

cp *.java $src/$group

touch $src/$sources
touch $target/META-INF/MANIFEST.MF

# iterating over files and appending package name;
for f in $(find $src/$group -name '*.java'); do 
	sed -i "1s/^/package $group_java;\n\n/" $f >> $f
	echo "$f" >> $src/$sources
done

javac -d $target @$src/$sources

echo "Manifest-Version: 1.0" >> $target/META-INF/MANIFEST.MF
echo "Main-Class: $group_java.Main" >> $target/META-INF/MANIFEST.MF

jar cmvf $target/META-INF/MANIFEST.MF Jifra.oldways.jar -C $target . 

java -jar Jifra.oldways.jar run

