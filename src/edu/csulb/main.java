package edu.csulb;

import cecs429.documents.*;
import cecs429.indexing.*;
import cecs429.queries.BooleanQueryParser;
import cecs429.queries.QueryComponent;
import cecs429.text.ComplexTokenProcessor;
import cecs429.text.EnglishTokenStream;
import mikera.vectorz.impl.SparseIndexedVector;
import org.tartarus.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class main {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
//        displayIndexOptions();
//        int input = userInput("input: ");
//        if (input==1){
//            String mainPath = getString("enter training set path: ");
//            String testPath = getString("enter test set path: ");
//            DirectoryCorpus corpus1 = IndexFromFile(mainPath,testPath);
//            Index index = indexCorpus(corpus1);
//            DirectoryCorpus corpus2 = IndexFromFile(testPath,"");
//            Index index2 = indexCorpus(corpus2);
//            System.exit(0);
//        }
//        if (input==2) {
//            String mainPath = getString("enter training set path: ");
//            String testPath = getString("enter test set path: ");
//            DirectoryCorpus corpus1 = IndexFromFile(mainPath, testPath);
//            DirectoryCorpus corpus2 = IndexFromFile(testPath, "");
//            corpus1.getDocuments();
//            corpus2.getDocuments();
//            DiskPositionalIndex main = new DiskPositionalIndex(corpus1.getmDirectoryPath().toString());
//            DiskPositionalIndex text = new DiskPositionalIndex(corpus2.getmDirectoryPath().toString());
//
//
        DirectoryCorpus corpus1 = IndexFromFile(args[0],args[4]);
        DirectoryCorpus corpus2 = IndexFromFile(args[4],"");
        corpus1.getDocuments();
        corpus2.getDocuments();
        DiskPositionalIndex index= new DiskPositionalIndex(corpus1.getmDirectoryPath().toString());
        DiskPositionalIndex index2= new DiskPositionalIndex(corpus2.getmDirectoryPath().toString());
        List<String> words =index.getVocabulary();
        HashMap<String,Integer> termid = new HashMap<>();
        int id=0;
        for (String w : words){
            termid.put(w,id++);
        }
        HashMap<Integer, SparseIndexedVector> id2vec = vecFromDoc(termid,index, corpus1.getCorpusSize());
        HashMap<Integer, SparseIndexedVector> testvec = vecFromDoc(termid,index2, corpus2.getCorpusSize());

        for (Map.Entry<Integer,SparseIndexedVector> es:testvec.entrySet()){
            List<Pair> nn = nearestNeighbors(id2vec, es.getValue());
            System.out.println(corpus2.getDocument(es.getKey()).getTitle());
            for (Pair p: nn.subList(0,5)){
                System.out.println(corpus1.getDocument(p.id).getTitle() +" ("+p.score+")");
            }
            System.out.println(findClass(corpus1,nn.subList(0,5)));
        }
    }

    public static List<Pair> nearestNeighbors(HashMap<Integer, SparseIndexedVector> id2vec,SparseIndexedVector test){
            List<Pair> result = new ArrayList<>();
            HashMap<Integer,Double> hashMap=new HashMap<>();
            for (Map.Entry<Integer,SparseIndexedVector> es:id2vec.entrySet()){
                hashMap.put(es.getKey(), es.getValue().distance(test));
            }

            PriorityQueue<Pair> pq = new PriorityQueue<>(Comparator.comparingDouble(o -> o.score));
            for (Map.Entry<Integer, Double> es:hashMap.entrySet()){
                if (es.getValue()!=0) {
                    pq.add(new Pair(es.getKey(), es.getValue() ));
                }
            }
            while (!pq.isEmpty()){
                result.add(pq.poll());
            }
            return result;
        }

        public static HashMap<Integer, SparseIndexedVector> vecFromDoc(HashMap<String,Integer> termid,DiskPositionalIndex index,int corpusSize) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
            HashMap<Integer, SparseIndexedVector> id2vec = new HashMap<>();
            ScoringStrategy defaultStrategy=new DefaultStrategy(index,corpusSize);
            for (int i=0;i<corpusSize;i++){
                id2vec.put(i,SparseIndexedVector.createLength(termid.size()));
            }
            for (String w : termid.keySet()){
                for (Posting p: index.getPostings(w)){
                    id2vec.get(p.getDocumentId()).addAt(termid.get(w), p.getTftd()*defaultStrategy.docTermWeight(p));
                }
            }
            for (Map.Entry<Integer,SparseIndexedVector> es:id2vec.entrySet()){
                es.getValue().normalise();
            }
            return id2vec;
        }

    static class Pair{
        public int id;
        public double score ;

        public Pair(int id, double score) {
            this.id = id;
            this.score = score;
        }
    }


    private static int userInput(String message) throws IOException {
        System.out.print(message);
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        userInput = br.readLine();
        int idx = Integer.parseInt(userInput);
        return idx;
    }

    private static String getString(String message) throws IOException {
        System.out.print(message);
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        userInput = br.readLine();
        return userInput;
    }

    private static double euclideanWeight(HashMap<String, Integer> hashMap){
        double ld=0;
        for (String k: hashMap.keySet()){
            int tftd = hashMap.get(k);
            double w = 1 + Math.log(tftd);
            ld+=w*w;
        }
        return Math.sqrt(ld);
    }
    private static double averageTftd(HashMap<String, Integer> hashMap){
        ArrayList<Integer> docs = new ArrayList<>();
        for (String k: hashMap.keySet()){
            int tftd = hashMap.get(k);
            docs.add(tftd);
        }
        return docs.stream().mapToInt(Integer::intValue).average().orElse(0.0);
    }

    private static void displayIndexOptions(){
        System.out.println("1. index corpus");
        System.out.println("2. knn");
    }

    private static Index indexCorpus(DocumentCorpus corpus) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SQLException {
        ComplexTokenProcessor processor = new ComplexTokenProcessor();
        PositionalInvertedIndex invertedIndex = new PositionalInvertedIndex();
        DiskIndexWriter indexWriter= new DiskIndexWriter(corpus.getmDirectoryPath().toAbsolutePath().toString());
        ArrayList<Integer> docs = new ArrayList<>();
        for (Document d : corpus.getDocuments()) {
            HashMap<String, Integer> tftd= new LinkedHashMap<>();
            EnglishTokenStream englishTokenStream = new EnglishTokenStream(d.getContent());
            int token = 1;
            for (String word : englishTokenStream.getTokens()) {
                List<String> strings=processor.processToken(word);
                for (String processedWord : strings) {
                    if (processedWord.trim().isEmpty()){
                        continue;
                    }
                    if (!tftd.containsKey(processedWord)){
                        tftd.put(processedWord, 1);
                    } else {
                        tftd.put(processedWord, tftd.get(processedWord)+1);
                    }
                    invertedIndex.addTerm(processedWord, d.getId(), token);
                }
                token++;
            }
            docs.add(token);

            double ld = euclideanWeight(tftd);
            indexWriter.setWeightsFile(d.getId(), ld);//euclidean
            indexWriter.setWeightsFile(d.getId(),token);//doclength
            indexWriter.setWeightsFile(d.getId(),averageTftd(tftd)); // average tftd
            indexWriter.setWeightsFile(d.getId(), Files.size(((FileDocument) d).getFilePath()));//byte size
        }
        double average = docs.stream().mapToInt(val -> val).average().orElse(0.0);
        indexWriter.setWeightsFile(0,average);// average doclength
        indexWriter.writeIndex(invertedIndex);
        indexWriter.close();
        return invertedIndex;
    }

    private static String findClass(DirectoryCorpus directoryCorpus, List<Pair> list){
        HashMap<String,Double> hm = new HashMap<>();
        for (String c : directoryCorpus.getClasses()){
            hm.put(c,0.0);
        }
        for (Pair p :list){
            double score = hm.getOrDefault(directoryCorpus.getClass(p.id),0.0);
            score+=1;
            hm.put(directoryCorpus.getClass(p.id),score);
        }
        return hm.entrySet().stream().max((entry1, entry2) -> entry1.getValue() > entry2.getValue() ? 1 : -1).get().getKey();

    }

    private static DirectoryCorpus IndexFromFile(String string, String ignore) throws IOException {
        if (isValidPath(string)){
            DirectoryCorpus directoryCorpus = new DirectoryCorpus(Path.of(string).toAbsolutePath(), s -> ignore.equals("") || !s.contains(ignore));
            directoryCorpus.registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);
            directoryCorpus.registerFileDocumentFactory(".txt", TextFileDocument::loadTextFileDocument);
            return directoryCorpus;
        }
        return findCorpus(false);
    }

    private static DirectoryCorpus findCorpus(boolean exclude) throws IOException {
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        do {
            System.out.print("Enter path of training se: ");
            userInput = br.readLine();
        } while (!isValidPath(userInput));
        if (exclude){
            String input;
            do {
                System.out.print("Enter DIR of test set: ");
                input= br.readLine();
            } while (!isValidPath(input));

            String finalInput = input;
            DirectoryCorpus directoryCorpus = new DirectoryCorpus(Path.of(userInput).toAbsolutePath(), s -> !s.contains(finalInput));
            directoryCorpus.registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);
            directoryCorpus.registerFileDocumentFactory(".txt", TextFileDocument::loadTextFileDocument);
            return directoryCorpus;
        }
        DirectoryCorpus directoryCorpus = new DirectoryCorpus(Path.of(userInput).toAbsolutePath());
        directoryCorpus.registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);
        directoryCorpus.registerFileDocumentFactory(".txt", TextFileDocument::loadTextFileDocument);
        return directoryCorpus;
    }
    private static boolean isValidPath(String path) {
        File f = new File(path);
        try {
            f.getCanonicalPath();
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
