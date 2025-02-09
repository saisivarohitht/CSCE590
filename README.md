# CACS590
Extraction and Analysis of an Online Graph Network

# Week 3 EXPLORE OFFLINE DBLP REPOSITORY

1. Download the following into a folder called ‘dblp’

    curl -O https://dblp.org/src/DblpExampleParser.java
    curl -O https://dblp.org/src/mmdb-2019-04-29.jar
    curl -O https://dblp.org/xml/release/dblp-2019-04-01.xml.gz
    curl -O https://dblp.org/xml/release/dblp-2017-08-29.dtd

2. Extract dblp-2019-04-01.xml.gz using ‘gunzip dblp-2019-04-01.xml.gz’
3. javac -cp mmdb-2019-04-29.jar DblpExampleParser.java
4. java -Xmx2G -cp mmdb-2019-04-29.jar:. DblpExampleParser dblp-2019-04-01.xml dblp-2017-08-29.dtd


References:
https://dblp.org/faq/index.html
https://dblp.uni-trier.de/xml/
https://dblp.org/faq/1474681.html
