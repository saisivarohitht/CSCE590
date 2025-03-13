package com.ull.graph.controller;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.dblp.mmdb.Person;
import org.dblp.mmdb.PersonName;
import org.dblp.mmdb.RecordDb;
import org.dblp.mmdb.RecordDbInterface;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.xml.sax.SAXException;

import com.ull.graph.model.Dblpperson;
import com.ull.graph.model.Publication;
import com.ull.graph.model.R;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.Unmarshaller;

@RestController
public class GraphController {
    static boolean debug = false;

    static Map<String, String> personNameIdMap = new TreeMap<String, String>();
    static List<String> personNamesList = new ArrayList<String>();
    static List<PersonName> personNamesObjList = new ArrayList<PersonName>();

    static List<Person> authorList = new ArrayList<Person>();
    
    static RecordDbInterface dblp;
    static String dblpXmlFilename = "dblp.xml.gz";
    static String dblpDtdFilename = "dblp.dtd";

    static {

        // Loading person details from csv file
        loadPersonDetailsFromCsvFle();

        if(personNameIdMap.size() == 0) {
            // Loading person details from dblp XML file
            loadPersonDetailsFromXmlFle();
        }
    }

    // Load person details from Persons.csv file (generated based on the data available in https://dblp.uni-trier.de/xml/dblp.xml.gz)
    static void loadPersonDetailsFromCsvFle() {
        try {
            long t1 = System.currentTimeMillis();
            BufferedReader br = new BufferedReader(new FileReader("Persons.csv"));
            String line =  null;
            while((line=br.readLine())!=null) {
                if(line.trim().length() > 0) {
                    String arr[] = line.split(",");
                    personNamesList.add(arr[0]);
                    personNameIdMap.put(arr[0], arr[1]);
                }
            }
            System.out.println("Loaded Persons data in " + (System.currentTimeMillis()- t1)/1000 + " seconds...");
            br.close();
        } catch (final IOException ex) {
            System.err.println("cannot read Persons.csv: " + ex.getMessage());
        }
    }

    // Load person details from dblp.xml.gz file (downloaded from https://dblp.uni-trier.de/xml/)
    static void loadPersonDetailsFromXmlFle() {

        if(dblp != null) return;
        System.setProperty("entityExpansionLimit", "10000000");
        System.out.println("building the dblp main memory DB ...");

        FileWriter personWriter = null;
        try {
            personWriter = new FileWriter("Persons.csv");
            long t1 = System.currentTimeMillis();
            dblp = new RecordDb(dblpXmlFilename, dblpDtdFilename, false);
            Collection<Person> persons = dblp.getPersons();
            Iterator<Person> iter = persons.iterator();
            while(iter.hasNext()) {
                Person person = iter.next();
                personNamesObjList.add(person.getPrimaryName());
                authorList.add(person);
                personNameIdMap.put(person.getPrimaryName().name(), person.getPid());
                personWriter.write(person.getPrimaryName() + "," + person.getPid()+"\n");
            }
            personWriter.close();
            System.out.println("Loaded in " + (System.currentTimeMillis()- t1)/1000 + " seconds...");
        }
        catch (final IOException ex) {
            System.err.println("cannot read dblp XML: " + ex.getMessage());
        }
        catch (final SAXException ex) {
            System.err.println("cannot parse XML: " + ex.getMessage());
        }
        System.out.format("MMDB ready: %d publs, %d pers\n\n", dblp.numberOfPublications(), dblp.numberOfPersons());
    }

    @GetMapping("/search/{name}")
    public List<String> searchMatchingNames(@PathVariable("name") String name) {
        System.out.println("search string: " + name + " No. of persons matching search string: " + personNamesList.size());
        if(name.equals("*")) {
            return personNamesList;
        }
        else {
        List<String> matchingElements = personNamesList.stream()
          .filter(str -> str.trim().contains(name))
          .collect(Collectors.toList());
        System.out.println("returning: " + matchingElements.size());
        return matchingElements;
        }
    }
    
    @PostMapping("/find")
    public ResponseEntity<Object> findGraph(@RequestBody String[] names) {
        if(debug) System.out.println(names.toString());
        List<Map> nodeArray = new ArrayList<Map>();
        List<String> pids = new ArrayList<String>();
        int i = 0;
        for(String name : names) {
            String pid = personNameIdMap.get(name);
            if(debug) System.out.println("name: " + name + " pid: " + pid);
            if(pid != null) {
                Map json = new HashMap();
                json.put("id", (i+1));
                json.put("name", name);
                nodeArray.add(json);
                pids.add(pid);
                i++;
            }
        }

        List<Map> linkArray = find(pids.toArray(new String[0]));

        Map graph = new HashMap();
        graph.put("nodes", nodeArray);
        graph.put("links", linkArray);
        if(debug) System.out.println("graph: " + graph.toString());
        return new ResponseEntity<>(graph, HttpStatus.OK);

    }

    private List<Map> find(String[] pids) {
        System.out.println("Finding links for " + pids.length + " persons...");
        // to store publications for every pid
        Map<String, List<Publication>> pidPublicationsMap = new HashMap<String, List<Publication>>();
        List<Map> graphArray = new ArrayList<Map>();
        long t1 = System.currentTimeMillis();
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(Dblpperson.class);
            Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
            for(int i =0; i < pids.length;i++) {
                // list to store publications for the current pid
                List<Publication> publications = new ArrayList<Publication>();

                // build URL for current person Id
                String urlString = "https://dblp.org/pid/" + pids[i] +".xml";

                // get XML for the person
                URL url = new URL(urlString);
                URLConnection urlConnection = url.openConnection();


                Dblpperson person = (Dblpperson) jaxbUnmarshaller.unmarshal(urlConnection.getInputStream());                
                if(debug) System.out.println("Person: " + pids[i] + " publication size: " + person.getR().length);
                for(R r : person.getR()) {
                    publications.add(r.getPublication());
                }
                pidPublicationsMap.put(pids[i], publications);
            }
            System.out.println("Time taken to extract the details (in secs): " + (System.currentTimeMillis()-t1)/1000);
        }
        catch(Exception ex) {
            ex.printStackTrace();

        }        t1 = System.currentTimeMillis();

        // to return response in a List of strings
        List<String> graph = new ArrayList<String>();

        // build graph
        //System.out.println("Graph:\n-----");
        for(int i = 0; i < pids.length-1;i++) {
            List<Publication> pubs_i = pidPublicationsMap.get(pids[i]);
            for(int j = i+1; j< pids.length; j++) {
                List<Publication> pubs_j = pidPublicationsMap.get(pids[j]);
                // check intersection
                List<Publication> intersectElements = pubs_i.stream()
                        .filter(pubs_j :: contains)
                        .collect(Collectors.toList());
                if(intersectElements.size() > 0) {
                    String edge = "" + (i+1) + "," + (j+1);
                    if(!graph.contains(edge)) {
                        if(debug) System.out.println(edge);
                        graph.add(edge);
                        Map json = new HashMap();
                        json.put("source", (i+1));
                        json.put("target", (j+1));
                        graphArray.add(json);
                    }
                }            
            }
        }
        if(graph.size() == 0) {
            System.out.println("No common edges found with the given input.");
        }

        System.out.println("Time taken to build graph (in secs): " + (System.currentTimeMillis()-t1)/1000);

        return graphArray;
    }

    // Write edges to graph
    public void writeGraphToFile(List<Map> linkList) {
        try {
            FileWriter myWriter = new FileWriter("Output.csv");
            if(debug) System.out.println("linkList.size(): " + linkList.size());
            for(Map linkMap : linkList) {
                if(debug) System.out.println("linkMap.size(): " + linkMap.size());
                StringBuilder link = new StringBuilder();
                link.append((Integer)linkMap.get("source"));
                link.append(",");
                link.append((Integer)linkMap.get("target"));
                if(debug) System.out.println(link.toString());
                myWriter.write(link.toString());
                myWriter.write("\n");
            }
            myWriter.close();
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }

    // read input
    public String[] readInputFromFile() {
        List<String> inList = new ArrayList<String>();
        try {
            File myObj = new File("Input.txt");
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String name = myReader.nextLine();
                if(name.trim().length() > 0) {
                    String id = personNameIdMap.get(name);
                    if(id.trim().length() > 0) {
                        inList.add(id);
                    } else {
                        System.out.println("Could not locate the pid for name: " + name);
                    }
                }
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return inList.toArray(new String[inList.size()]);
    }

    public static void generateAuthorsGraphFromOnlineDblpDB() {
        GraphController instance = new GraphController();
        String [] pids = instance.readInputFromFile();
        List<Map> graph = instance.find(pids);
        if(graph.size() > 0) {
            instance.writeGraphToFile(graph);
            System.out.println("Graph is generated. Check Output.csv file.");
        }
    }
    
    public static void generateAuthorsGraphFromOfflineDblpDB() {

        loadPersonDetailsFromXmlFle();

        FileWriter myWriter = null;
        try {
            myWriter = new FileWriter("AuthorGraph.csv");
        long t1 = System.currentTimeMillis();
            List<Person> persons = new ArrayList<Person>();
            long iIndex = 0;
            System.out.println("authorList.size(): " + authorList.size());
            //Map<List<String>, List<String>> coAuthorMap = new HashMap<List<String>, List<String>>();
            Collection<org.dblp.mmdb.Publication> publications = new HashSet<org.dblp.mmdb.Publication>();
            for(Person author : authorList) {
//                Person per = dblp.getPerson(authorName);
//                persons.add(per);
                Collection<org.dblp.mmdb.Publication> personPublications = dblp.getPublications(author);
                //System.out.println("authorName: " + authorName + " publication size: " + personPublications.size());
                publications.addAll(personPublications);
                /*
                Collection<Person> coAuthors = dblp.getCoauthors(per);
                //System.out.println(iIndex + " : " + coAuthors.size());
                Iterator<Person> iter = coAuthors.iterator();
                while(iter.hasNext()) {
                    Person coAuthor = iter.next();
                    long jIndex = authorList.indexOf(coAuthor);
                    //System.out.println((iIndex++) + "," + (jIndex++) + coAuthor.getPrimaryName());
                    StringBuilder outStr = new StringBuilder();
                    if(jIndex > iIndex) {
                        outStr.append(iIndex+1).append(",").append(jIndex+1);
                    }
                    else if(iIndex > jIndex) {
                        outStr.append(jIndex+1).append(",").append(iIndex+1);
                    }
                    if(outStr.length() > 0) {
                        myWriter.write(outStr.toString());
                        myWriter.write("\n");
                    }
                }*/
                iIndex++;
                //if(iIndex % 1000 == 0) break;
            }
            System.out.println("Total publications size: " + publications.size());
            Set<String> authorCombinations = new HashSet<String>();
            int lIndex = 0;
            for(org.dblp.mmdb.Publication pub : publications) {
                List<PersonName> personNames = pub.getNames();
                if(debug) System.out.println("personNames.size(): " + personNames.size());
                List<Integer> userKeys = new ArrayList<Integer>();
                //for(int k=0; k < personNamesList.size(); k++) {
                    for(PersonName personName : personNames) {
                        int k = personNamesObjList.indexOf(personName);
                        if(k >= 0) userKeys.add(k);
//                        System.out.println(personName.getPrimaryName().getPrimaryName());
//                        if(personName.getPrimaryName().getPrimaryName().equals(personNamesList.get(k).getPrimaryName())) {
//                            System.out.println(k);
//                            usersKey.add(k);
//                            break;
//                        }
                    }

                //}
                if(debug) System.out.println(pub.getKey() + " usersKey.size(): " + userKeys.size());
                if(userKeys.size() > 1)
                {
                    for(int i=0; i< userKeys.size(); i++) {
                        for(int j=i+1; j< userKeys.size(); j++) {
                            StringBuilder edge = new StringBuilder();
                            edge.append(userKeys.get(i)+1).append(",").append(userKeys.get(j)+1);
                            boolean added = authorCombinations.add(edge.toString());
//                            if(added) {
//                                if(debug) System.out.println(edge.toString());
//                                myWriter.write(edge.toString());
//                                myWriter.write("\n");
//                            }
                        }
                    }
                }
                lIndex++;
                //if(lIndex % 10000 == 0) break;
                if(debug) System.out.println("publication: " + (lIndex+1));
            }
            System.out.println("authorCombinations.size(): " + authorCombinations.size());
            Iterator<String> iter = authorCombinations.iterator();
            while(iter.hasNext()) {
                myWriter.write(iter.next());
                myWriter.write("\n");
            }

            System.out.println("Time taken to generate graph (in secs): " + (System.currentTimeMillis()-t1)/1000);
            
        } catch (IOException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        finally {
            try {
                if(myWriter != null) myWriter.close();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
    }

    public static void generatePublicationsCountsPerYearFromOfflineDblpDB() {
        loadPersonDetailsFromXmlFle();
        
        Collection<org.dblp.mmdb.Publication> publications = dblp.getPublications();
        Iterator<org.dblp.mmdb.Publication> iter = publications.iterator();
        TreeMap<Integer, Integer> publicationsByYear = new TreeMap<Integer, Integer>();
        while(iter.hasNext()) {
            org.dblp.mmdb.Publication pub = iter.next();
            int year = pub.getYear();
            Integer pubCount = publicationsByYear.get(year);
            if(pubCount == null) {
                pubCount = 0;
            }
            publicationsByYear.put(year, ++pubCount);
        }
        
        Iterator<Integer> pubIter = publicationsByYear.keySet().iterator();
        while(pubIter.hasNext()) {
           int year = pubIter.next();
           int count = publicationsByYear.get(year);
           System.out.println(year + " " + count);
        }
    }

    public static void main(String [] args) {
        //generateAuthorsGraphFromOnlineDblpDB();
    	if(args.length > 2) {
    		generatePublicationsCountsPerYearFromOfflineDblpDB();
    		generateAuthorsGraphFromOfflineDblpDB();
    	}
    }
}
