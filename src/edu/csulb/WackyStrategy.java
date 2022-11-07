package edu.csulb;

import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.Posting;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class WackyStrategy implements ScoringStrategy{
    private final int N;
    private final DiskPositionalIndex index;

    public WackyStrategy(DiskPositionalIndex index, int N){
        this.index = index;
        this.N = N;
    }
    @Override
    public double getLength(int docId) throws IOException {
        return Math.sqrt(index.byteSize(docId));
    }

    @Override
    public double queryTermWeight(String term) throws SQLException, IOException {
        List<Posting> postings= index.getPostings(term);
        return Math.max(0,Math.log((N- postings.size())*1.0/ postings.size()));
    }

    @Override
    public double docTermWeight(Posting p) throws IOException {
        return (1+Math.log(p.getTftd()))/(1+Math.log(index.avgTftd(p.getDocumentId())));
    }
}
