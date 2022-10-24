package edu.csulb;

import cecs429.documents.*;
import cecs429.indexing.DiskIndexWriter;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.queries.BooleanQueryParser;
import cecs429.queries.QueryComponent;
import cecs429.text.ComplexTokenProcessor;
import cecs429.text.EnglishTokenStream;
import org.tartarus.snowball.SnowballStemmer;

import java.io.*;
import java.nio.file.Path;
import java.sql.SQLException;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class InvertedIndexIndexer {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException {
        // Create a DocumentCorpus to load .txt documents from the project directory.
         DirectoryCorpus corpus = findCorpus();
        // Index the documents of the corpus.
        Instant start = Instant.now();
        Index index = indexCorpus(corpus);
        Instant end = Instant.now();
        Duration timeElapsed = Duration.between(start, end);
        System.out.println("Time taken: "+ timeElapsed.toSeconds() +" seconds");
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
                    start = Instant.now();
                    index = indexCorpus(corpus);
                    end = Instant.now();
                    timeElapsed = Duration.between(start, end);
                    System.out.println("Time taken: "+ timeElapsed.toSeconds() +" seconds");
                }else if (strings[0].equals(":stem")){
                    System.out.println(Stem(strings[1]));
                }
            }
            else {
                QueryComponent component= parser.parseQuery(userInput);
                start = Instant.now();
                List<Posting> postings = component.getPostings(index);
                end = Instant.now();
                timeElapsed = Duration.between(start, end);
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
            System.out.print("\n\n\n");}
        } while (!userInput.equals(":q"));
    }

    private static void readDocument(Document document) throws IOException {
BufferedReader reader = new BufferedReader(document.getContent());
        String line;
        while ((line = reader.readLine()) != null) {
            System.out.println(line);
        }
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

private static void displayIndexOptions(){
        System.out.println("1. index corpus");
    System.out.println("2. query corpus");
}

private static void displayQueryOption(){
    System.out.println("1. ranked retrieval");
    System.out.println("2. boolean retrieval");

}
    private static Index indexCorpus(DocumentCorpus corpus) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SQLException {
        ComplexTokenProcessor processor = new ComplexTokenProcessor();
        PositionalInvertedIndex invertedIndex = new PositionalInvertedIndex();
        DiskIndexWriter indexWriter= new DiskIndexWriter();
        for (Document d : corpus.getDocuments()) {
            HashMap<String, Integer> tftd= new LinkedHashMap<>();
            EnglishTokenStream englishTokenStream = new EnglishTokenStream(d.getContent());
            int token = 1;
            for (String word : englishTokenStream.getTokens()) {
                List<String> strings=processor.processToken(word);
                for (String processedWord : strings) {
                    if (!tftd.containsKey(processedWord)){
                        tftd.put(processedWord, 1);
                    } else {
                        tftd.put(processedWord, tftd.get(processedWord)+1);
                    }
                    invertedIndex.addTerm(processedWord, d.getId(), token);
                }
                token++;
            }
            double ld = euclideanWeight(tftd);
            indexWriter.setWeightsFile(d.getId(), ld);
        }
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
        return findCorpus();
    }
    private static void Vocab(Index index){
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

    private static DirectoryCorpus findCorpus() throws IOException {
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        do {
            System.out.print("Enter path: ");
            userInput = br.readLine();
        } while (!isValidPath(userInput));
       DirectoryCorpus directoryCorpus = new DirectoryCorpus(Path.of(userInput));
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