package cecs429.indexing;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

/**
 * An Index can retrieve postings for a term from a data structure associating terms and the documents
 * that contain them.
 */
public interface Index {
	/**
	 * Retrieves a list of Postings of documents that contain the given term.
	 */
	List<Posting> getPostings(String term) throws IOException, SQLException;


	List<Posting> getPostingsWithoutPos(String term) throws IOException, SQLException;

	double getEuclideanWeight(int docID) throws IOException ;
	double avgTftd(int docID) throws IOException;

	double getDocLength(int docID) throws IOException;

	double getAvgDocLength() throws IOException;

	double byteSize(int d) throws IOException;
	/**
	 * A sorted list of all terms in the index vocabulary.
	 */
	List<String> getVocabulary();
}
