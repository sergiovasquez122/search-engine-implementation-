package cecs429.queries;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.ComplexTokenProcessor;

/**
 * A TermLiteral represents a single term in a subquery.
 */
public class TermLiteral implements QueryComponent {
	private String mTerm;
	
	public TermLiteral(String term) {
		mTerm = term;
	}
	
	public String getTerm() {
		return mTerm;
	}
	
	@Override
	public List<Posting> getPostings(Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
		ComplexTokenProcessor processor = new ComplexTokenProcessor();
		List<String> strings=processor.processToken(mTerm);
		return index.getPostingsWithoutPos(strings.get(strings.size()-1));
	}
	
	@Override
	public String toString() {
		return mTerm;
	}
}
