package edu.csulb;

import cecs429.indexing.Posting;

import java.io.IOException;
import java.sql.SQLException;

public interface ScoringStrategy {
    double getLength(int docId) throws IOException;

    double queryTermWeight(String term) throws SQLException, IOException;

    double docTermWeight(Posting p) throws IOException;
}
