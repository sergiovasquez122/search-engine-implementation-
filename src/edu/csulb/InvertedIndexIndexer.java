package edu.csulb;

import cecs429.documents.*;
import cecs429.indexing.Index;
import cecs429.indexing.PositionalInvertedIndex;
import cecs429.indexing.Posting;
import cecs429.text.BasicTokenProcessor;
import cecs429.text.EnglishTokenStream;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;

public class InvertedIndexIndexer {
    public static void main(String[] args) throws IOException, ClassNotFoundException {
        // Create a DocumentCorpus to load .txt documents from the project directory.
        DocumentCorpus corpus = findCorpus();
        // Index the documents of the corpus.
        Index index = indexCorpus(corpus);

        // We aren't ready to use a full query parser; for now, we'll only support single-term queries.
        String query = "whale"; // hard-coded search for "whale"
        for (Posting p : index.getPostings(query)) {
            System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
        }
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        do {
            System.out.print("Enter query: ");
            userInput = br.readLine();
            for (Posting p : index.getPostings(userInput)) {
                System.out.println("Document " + corpus.getDocument(p.getDocumentId()).getTitle());
            }
            System.out.print("\n\n\n");
        } while (!userInput.equals("quit"));
    }

    private static Index indexCorpus(DocumentCorpus corpus) {
        BasicTokenProcessor processor = new BasicTokenProcessor();

        PositionalInvertedIndex invertedIndex = new PositionalInvertedIndex();
        for (Document d : corpus.getDocuments()) {
            EnglishTokenStream englishTokenStream = new EnglishTokenStream(d.getContent());
            int token = 1;
            for (String word : englishTokenStream.getTokens()) {
                invertedIndex.addTerm(processor.processToken(word), d.getId(), token++);
            }
        }
        return invertedIndex;
    }

    private static DirectoryCorpus findCorpus() throws IOException {
        InputStreamReader inp = new InputStreamReader(System.in);
        BufferedReader br = new BufferedReader(inp);
        String userInput;
        do {
            System.out.print("Enter path: ");
            userInput = br.readLine();
            System.out.print("\n\n\n");
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