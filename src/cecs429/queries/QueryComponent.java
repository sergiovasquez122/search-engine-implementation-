package cecs429.queries;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import cecs429.indexing.*;

/**
 * A QueryComponent is one piece of a larger query, whether that piece is a literal string or represents a merging of
 * other components. All nodes in a query parse tree are QueryComponent objects.
 */
public interface QueryComponent {
    /**
     * Retrieves a list of postings for the query component, using an Index as the source.
     */
    List<Posting> getPostings(Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SQLException;

    boolean getSign();

    void setSign(boolean sign);
}