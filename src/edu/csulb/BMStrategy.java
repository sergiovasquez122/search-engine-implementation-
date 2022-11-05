package edu.csulb;

import cecs429.indexing.DiskPositionalIndex;
import cecs429.indexing.Posting;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

public class BMStrategy implements ScoringStrategy{
    private DiskPositionalIndex index;
    private int N;

    public BMStrategy(DiskPositionalIndex index, int N){
        this.index = index;
        this.N = N;
    }
    @Override
    public double getLength(int docId) throws IOException {
        return 1;
    }

    @Override
    public double queryTermWeight(String term) throws SQLException, IOException {
        List<Posting> postings= index.getPostings(term);
        return Math.max(.1, Math.log((N- postings.size()+.5) / (postings.size())+.5));
    }

    @Override
    public double docTermWeight(Posting p) throws IOException {
        return (2.2 * p.getTftd())/(1.2*(0.25+0.75*(index.getDocLength(p.getDocumentId())/index.getAvgDocLength()))+ p.getTftd());
    }
}
