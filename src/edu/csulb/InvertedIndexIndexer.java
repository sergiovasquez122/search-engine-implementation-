package edu.csulb;

import cecs429.documents.*;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;
import org.tartarus.snowball.SnowballStemmer;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class InvertedIndexIndexer {
    public static void main(String[] args) throws IOException, ClassNotFoundException, InstantiationException, IllegalAccessException {
        // Create a DocumentCorpus to load .txt documents from the project directory.
         DirectoryCorpus corpus = findCorpus();
        // Index the documents of the corpus.
        Index index = indexCorpus(corpus);
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        do {
            System.out.print("Enter query: ");
            userInput = br.readLine();
            String trimmed = userInput.trim();
            if (trimmed.startsWith(":"))
            {
                SpecialQuery(trimmed,corpus,index);
            }
            else {for (Posting p : index.getPostings(userInput)) {
                System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
            }
            System.out.print("\n\n\n");}
        } while (!userInput.equals("quit"));
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
        BasicTokenProcessor processor = new BasicTokenProcessor();

        PositionalInvertedIndex invertedIndex = new PositionalInvertedIndex();
        for (Document d : corpus.getDocuments()) {
            EnglishTokenStream englishTokenStream = new EnglishTokenStream(d.getContent());
            int token = 1;
            for (String word : englishTokenStream.getTokens()) {
                for (String processedWord : processor.processToken(word)) {
                    invertedIndex.addTerm(processedWord, d.getId(), token);
                }
                token++;
            }
        }
        return invertedIndex;
    }

    private static void SpecialQuery(String input,DirectoryCorpus corpus, Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException {
        switch (input) {
            case ":q" -> System.exit(0);
            case ":vocab" -> Vocab(index);
        }
      String[] strings = input.split(" ");
        if (strings[0].equals(":index")){
            corpus = IndexFromFile(strings[1]);
            index = indexCorpus(corpus);
        }else if (strings[0].equals(":stem")){
            System.out.println(Stem(strings[1]));
        }
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