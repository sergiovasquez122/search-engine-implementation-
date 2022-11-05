package edu.csulb;

import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.Posting;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class TfStrategy implements ScoringStrategy {
    private final int N;
    private final DiskPositionalIndex index;

    public TfStrategy(DiskPositionalIndex index, int N){
        this.index = index;
        this.N = N;
    }
    @Override
    public double getLength(int docId) throws IOException {
        return index.getEuclideanWeight(docId);
    }

    @Override
    public double queryTermWeight(String term) throws SQLException, IOException {
        List<Posting> postings= index.getPostings(term);
        return Math.log(N*1.0 / postings.size());
    }

    @Override
    public double docTermWeight(Posting p) {
        return p.getTftd();
    }
}
