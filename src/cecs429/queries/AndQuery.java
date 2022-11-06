package cecs429.queries;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * An AndQuery composes other QueryComponents and merges their postings in an intersection-like operation.
 */
public class AndQuery implements QueryComponent {
	private List<QueryComponent> mComponents;

	public AndQuery(List<QueryComponent> components) {
		mComponents = components;
	}
	
	@Override
	public List<Posting> getPostings(Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SQLException {
		int idx = 1;
		QueryComponent q1 = mComponents.get(0);
		List<Posting> result= mComponents.get(0).getPostings(index);
		while (idx< mComponents.size() )
		{
			QueryComponent q2 = mComponents.get(idx);
			List<Posting> l2= mComponents.get(idx).getPostings(index);
			if (q1.getSign()||q2.getSign()){
				if (q1.getSign()){
					result=notIntersect(l2,result);
					q1.setSign(false);
				}else {
					result=notIntersect(result,l2);
				}
			}else {
				result = intersect(result, mComponents.get(idx).getPostings(index));
			}
			idx++;
		}
		return result;
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

				l1_idx++;
			}else {
				l2_idx++;
			}
		}
		return result;
	}

	private static List<Posting> notIntersect (List<Posting> l1, List<Posting> l2){
		List<Posting> result = new ArrayList<>();
		int l1_idx =0;
		int l2_idx =0;
		while (l1_idx<l1.size()&&l2_idx<l2.size()){
			Posting p1=l1.get(l1_idx);
			Posting p2=l2.get(l2_idx);
			if (p1.getDocumentId()== p2.getDocumentId()){
				l1_idx++;
				l2_idx++;
			} else if (p1.getDocumentId()< p2.getDocumentId()){
				result.add(p1);
				l1_idx++;
			}else {
				l2_idx++;
			}
		}
		while (l1_idx<l1.size()){
			Posting p1=l1.get(l1_idx);
			result.add(p1);
			l1_idx++;
		}
		return result;
	}
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
