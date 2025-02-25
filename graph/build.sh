source ~/java23.sh
./gradlew clean build -x test
java -jar -Xmx8G build/libs/graph-0.0.1-SNAPSHOT.jar
