package cecs429.queries;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An OrQuery composes other QueryComponents and merges their postings with a union-type operation.
 */
public class OrQuery implements QueryComponent {
	// The components of the Or query.
	private List<QueryComponent> mComponents;
	
	public OrQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
		public List<Posting> getPostings(Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
			int idx = 1;
			List<Posting> result= mComponents.get(0).getPostings(index);
			while (idx< mComponents.size() )
			{
				result = intersect(result, mComponents.get(idx).getPostings(index));
				idx++;
			}
			return result;
		}
	private static List<Posting> intersect (List<Posting> l1, List<Posting> l2){
		List<Posting> result = new ArrayList<>();
		int l1_idx =0;
		int l2_idx =0;
		while (l1_idx<l1.size()&&l2_idx<l2.size()){
			Posting p1=l1.get(l1_idx);
			Posting p2=l2.get(l2_idx);
			if (p1.getDocumentId()== p2.getDocumentId()){
				result.add(p1);
				l1_idx++;
				l2_idx++;
			} else if (p1.getDocumentId()< p2.getDocumentId()){
				result.add(p1);
				l1_idx++;
			}else {
				result.add(p2);
				l2_idx++;
			}
		}
		while (l1_idx<l1.size())result.add(l1.get(l1_idx++));
		while (l2_idx<l2.size())result.add(l2.get(l2_idx++));
		return result;
	}

	@Override
	public String toString() {
		// Returns a string of the form "[SUBQUERY] + [SUBQUERY] + [SUBQUERY]"
		return "(" +
		 String.join(" + ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()))
		 + " )";
	}
	private boolean sign = false;
	@Override
	public boolean getSign() {
		return sign;
	}

	@Override
	public void setSign(boolean sign) {
		this.sign=sign;
	}
}
