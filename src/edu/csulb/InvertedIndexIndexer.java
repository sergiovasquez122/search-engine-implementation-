package edu.csulb;

import cecs429.documents.*;
import cecs429.indexing.*;
import cecs429.queries.BooleanQueryParser;
import cecs429.queries.QueryComponent;
import cecs429.text.ComplexTokenProcessor;
import cecs429.text.EnglishTokenStream;
import org.tartarus.snowball.SnowballStemmer;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.function.Predicate;

public class InvertedIndexIndexer {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        displayIndexOptions();
        int input = userInput("input: ");
        if (input==3){

            DirectoryCorpus corpus = findCorpus(true);
            Instant start = Instant.now();
            Index index = indexCorpus(corpus);
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            System.out.println("Time taken: "+ timeElapsed.toSeconds() +" seconds");
            System.exit(0);
        }
        DirectoryCorpus corpus = findCorpus(false);
        if (input==1){
            Instant start = Instant.now();
            Index index = indexCorpus(corpus);
            Instant end = Instant.now();
            Duration timeElapsed = Duration.between(start, end);
            System.out.println("Time taken: "+ timeElapsed.toSeconds() +" seconds");
            System.exit(0);
        }
        displayQueryOption();
        input = userInput("input: ");
        
        Index index;
            index = new DiskPositionalIndex(corpus.getmDirectoryPath().toAbsolutePath().toString());
            corpus.getDocuments();
            int mode;
            ScoringStrategy strategy = null;
            if (input==1){
                mode = userInput("1. default or 2. tfidf or 3. BM or 4. Wacky: ");
                strategy= scoringStrategyHashMap((DiskPositionalIndex) index,corpus).get(mode);
            }
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        BooleanQueryParser parser=new BooleanQueryParser();
        do {
            System.out.print("Enter query: ");
            userInput = br.readLine();
            String trimmed = userInput.trim();
            if (trimmed.startsWith(":"))
            {
                switch (trimmed) {
                    case ":q" -> System.exit(0);
                    case ":vocab" -> Vocab(index);
                }
                String[] strings = trimmed.split(" ");
                if (strings[0].equals(":index")){
                    corpus = IndexFromFile(strings[1]);
                    Instant start = Instant.now();
                    index = indexCorpus(corpus);
                    Instant end = Instant.now();
                    Duration timeElapsed = Duration.between(start, end);
                    System.out.println("Time taken: "+ timeElapsed.toSeconds() +" seconds");
                    System.exit(0);
                }else if (strings[0].equals(":stem")){
                    System.out.println(Stem(strings[1]));
                }
            }
            else if (input==2){
                QueryComponent component= parser.parseQuery(userInput);
                Instant start = Instant.now();
                List<Posting> postings = component.getPostings(index);
                Instant end = Instant.now();
                Duration timeElapsed = Duration.between(start, end);
                for (int i=0;i<postings.size();i++){
                    Posting p = postings.get(i);
                    System.out.println("Document " + i + ": "+ corpus.getDocument(p.getDocumentId()).getTitle());
                }
                System.out.println(postings.size());
                System.out.println("Time taken: "+ timeElapsed.toMillis() +" milliseconds");
                if (!postings.isEmpty()) {
                    System.out.print("Enter docid to view or -1 to skip: ");
                    userInput = br.readLine();
                    trimmed = userInput.trim();
                    int idx = Integer.parseInt(trimmed);
                    if (idx != -1) {
readDocument(corpus.getDocument(idx));
                    }
                }
            System.out.print("\n\n\n");
            }
            else if (input==1){
                List<Pair> postings= termAtATime(userInput,index,strategy);
                for (int i=0;i<postings.size();i++){
                    Pair p = postings.get(i);
                    System.out.print("Document " + i + ": "+ corpus.getDocument(p.id).getTitle());
                    System.out.println(" score: "+p.score);
                }
                System.out.println(postings.size());
                if (!postings.isEmpty()) {
                    System.out.print("Enter docid to view or -1 to skip: ");
                    userInput = br.readLine();
                    trimmed = userInput.trim();
                    int idx = Integer.parseInt(trimmed);
                    if (idx != -1) {
                        readDocument(corpus.getDocument(idx));
                    }
                }
            }
        } while (!userInput.equals(":q"));
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

    private static void readDocument(Document document) throws IOException {
BufferedReader reader = new BufferedReader(document.getContent());
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
}

private static HashMap<Integer,ScoringStrategy> scoringStrategyHashMap(DiskPositionalIndex index,DirectoryCorpus c){
    HashMap<Integer,ScoringStrategy> hashMap=new HashMap<>();
    hashMap.put(1,new DefaultStrategy(index, c.getCorpusSize()));
    hashMap.put(2,new TfStrategy(index, c.getCorpusSize()));
    hashMap.put(3, new BMStrategy(index,c.getCorpusSize()));
    hashMap.put(4, new WackyStrategy(index,c.getCorpusSize()));
    return hashMap;
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
    System.out.println("2. query corpus");
    System.out.println("3. knn");
}

private static void displayQueryOption(){
    System.out.println("1. ranked retrieval");
    System.out.println("2. boolean retrieval");

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

    private static DirectoryCorpus IndexFromFile(String string) throws IOException {
        if (isValidPath(string)){
                    DirectoryCorpus directoryCorpus = new DirectoryCorpus(Path.of(string));
            directoryCorpus.registerFileDocumentFactory(".json", JsonFileDocument::loadJsonFileDocument);
            directoryCorpus.registerFileDocumentFactory(".txt", TextFileDocument::loadTextFileDocument);
            return directoryCorpus;
        }
        return findCorpus(false);
    }
    private static void Vocab(Index index) throws SQLException {
        List<String> terms = index.getVocabulary();
        for (int i=0;i<1000;i++){
            System.out.println(terms.get(i));
        }
        System.out.println(terms.size());
    }

    private static String Stem(String term) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class stemClass = Class.forName("org.tartarus.snowball.ext." + "englishStemmer");
        SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
        stemmer.setCurrent(term);
        stemmer.stem();
        return stemmer.getCurrent();
    }
    
    private static class Pair{
        public int id;
        public double score ;

        public Pair(int id, double score) {
            this.id = id;
            this.score = score;
        }
    }

    private static List<Pair> termAtATime(String query, Index index, ScoringStrategy strategy) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
        List<Pair> result = new ArrayList<>();
        HashMap<Integer,Double> hashMap=new HashMap<>();
        ComplexTokenProcessor processor = new ComplexTokenProcessor();
        for (String term: query.split(" ")) {
            List<String> strings = processor.processToken(term);
            String word = strings.get(strings.size()-1);
            double wqt = strategy.queryTermWeight(word);
            for (Posting p:index.getPostings(word)){
                double wdt = strategy.docTermWeight(p);
                double ad = hashMap.getOrDefault(p.getDocumentId(),0.0);
                ad+=wqt*wdt;
                hashMap.put(p.getDocumentId(),ad);
            }
        }

        PriorityQueue<Pair> pq = new PriorityQueue<>((o1, o2) -> Double.compare(o2.score,o1.score));
        for (Map.Entry<Integer, Double> es:hashMap.entrySet()){
            if (es.getValue()!=0) {
                pq.add(new Pair(es.getKey(), es.getValue() / strategy.getLength(es.getKey())));
            }
        }
        while (!pq.isEmpty()){
            result.add(pq.poll());
        }
        return result;
    }

    private static DirectoryCorpus findCorpus(boolean exclude) throws IOException {
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        do {
            System.out.print("Enter path: ");
            userInput = br.readLine();
        } while (!isValidPath(userInput));
        if (exclude){
            String input;
            do {
                System.out.print("Enter path: ");
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