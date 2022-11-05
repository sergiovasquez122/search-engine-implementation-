package edu.csulb;

import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.Posting;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class DefaultStrategy implements ScoringStrategy{
    private DiskPositionalIndex index;
    private int N;

    public DefaultStrategy(DiskPositionalIndex index, int N){
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
       return Math.log(1 + ((N*1.0) / postings.size()));
    }

    @Override
    public double docTermWeight(Posting p) {
        return 1+Math.log(p.getTftd());
    }
}
