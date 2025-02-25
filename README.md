# CACS590
Extraction and Analysis of an Online Graph Network

# Week 3 EXPLORE OFFLINE DBLP REPOSITORY

1. Download the following into a folder called ‘dblp’

    curl -O https://dblp.org/src/DblpExampleParser.java \
    curl -O https://dblp.org/src/mmdb-2019-04-29.jar \
    curl -O https://dblp.org/xml/release/dblp-2019-04-01.xml.gz \
    curl -O https://dblp.org/xml/release/dblp-2017-08-29.dtd

2. Extract dblp-2019-04-01.xml.gz using ‘gunzip dblp-2019-04-01.xml.gz’
3. javac -cp mmdb-2019-04-29.jar DblpExampleParser.java
4. java -Xmx2G -cp mmdb-2019-04-29.jar:. DblpExampleParser dblp-2019-04-01.xml dblp-2017-08-29.dtd


References:
https://dblp.org/faq/index.html
https://dblp.uni-trier.de/xml/
https://dblp.org/faq/1474681.html

# Week 4 & Week 5: PROTOTYPE WITH PROSPECTIVE TOOLS & FRAMEWORK 

**Setup:**

1. Create a Spring Application from https://start.spring.io/ and generate graph.zip
2. Extract graph.zip file to a folder /home/graph/
3. ./gradlew clean build
4. Update the contents of https://github.com/saisivarohith-tamu/CACS590/graph in /home/graph/
5. Copy https://dblp.org/xml/release/dblp-2019-04-01.xml.gz as dblp.xml.gz to /home/graph/


**How to build the code:**

cd /home/graph
Make sure JAVA_HOME is set to build 23.0.2
./gradlew clean build

**Execute the code in offline mode**

cd /home/graph
java -Xmx4g -cp ".:libs/*:build/libs/*" com.ull.graph.controller.GraphController
Make sure /home/graph/Persons.csv file is created

**Start Spring Boot server (online mode)**

java -jar -Xmx8G build/libs/graph-0.0.1-SNAPSHOT.jar

**APIs**

**Find API** \
curl -X POST "http://localhost:8080/find" -H "Content-Type: application/json" -d '["Aygun, Sercan", "Sercan Aygun", "Martin Margala","Chaudhry, Beenish Moalla", "Li Chen", "Sheng Chen", "Chee-Hung Henry Chu", "Shuvalaxmi Dass", "Ramesh Kolluru", "Anthony Maida" ]'

**Search API** \
curl -X GET "http://localhost:8080/search/Michael%20W%20Totaro" \
curl -X GET "http://localhost:8080/search/Martin%20Margala" \
curl -X GET "http://localhost:8080/search/Ramesh%20Kolluru"

