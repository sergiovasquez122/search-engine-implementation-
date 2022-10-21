package cecs429.indexing;


import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class PositionalInvertedIndex implements Index{
    private HashMap<String, List<Posting>>  termToDocID = new HashMap<>();

    @Override
    public List<Posting> getPostings(String term) {
        if(termToDocID.containsKey(term)){
            return termToDocID.get(term);
        }
        return new ArrayList<>();
    }

    @Override
    public List<Posting> getPostingsWithoutPos(String term) throws IOException {
        return null;
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
    public void addTerm(String term, int documentId, int pos) {
        if(!termToDocID.containsKey(term)){
            List<Posting> docIDs = new ArrayList<>();
            termToDocID.put(term, docIDs);
            Posting posting = new Posting(documentId);
            posting.addPos(pos);
            docIDs.add(posting);
        } else {
            List<Posting> docsIDs = termToDocID.get(term);
            if(docsIDs.get(docsIDs.size() - 1).getDocumentId() != documentId){
                Posting posting = new Posting(documentId);
                posting.addPos(pos);
                docsIDs.add(posting);
            }else {
                docsIDs.get(docsIDs.size()-1).addPos(pos);
            }
        }
    }
}
