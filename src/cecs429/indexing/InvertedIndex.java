package cecs429.indexing;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class InvertedIndex implements Index{
    private HashMap<String, List<Posting>>  termToDocID = new HashMap<>();
    @Override
    public List<Posting> getPostings(String term) {
        if(termToDocID.containsKey(term)){
            return termToDocID.get(term);
        }
        return new ArrayList<>();
    }

    @Override
    public List<String> getVocabulary() {
        List<String> keys = new ArrayList<>(termToDocID.keySet());
        Collections.sort(keys);
        return keys;
    }
    /**
     * Associates the given documentId with the given term in the index.
     */
    public void addTerm(String term, int documentId) {
        if(!termToDocID.containsKey(term)){
            List<Posting> docIDs = new ArrayList<>();
            termToDocID.put(term, docIDs);
            docIDs.add(new Posting(documentId));
        } else {
            List<Posting> docsIDs = termToDocID.get(term);
            if(docsIDs.get(docsIDs.size() - 1).getDocumentId() != documentId){
                docsIDs.add(new Posting(documentId));
            }
        }
    }
}
