package cecs429.queries;

import java.util.*;
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
	public List<Posting> getPostings(Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		List<List<Posting>> postings = new ArrayList<>();
		for (QueryComponent component : mComponents){
			postings.add(component.getPostings(index));
		}
		postings.sort(Comparator.comparingInt(List::size));
		int idx = 1;
		List<Posting> result= postings.get(0);
		while (idx<  postings.size() && !result.isEmpty())
		{
			result = intersect(result, postings.get(idx));
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

				l1_idx++;
			}else {
				l2_idx++;
			}
		}
		return result;
	}


	
	@Override
	public String toString() {
		return
		 String.join(" ", mComponents.stream().map(c -> c.toString()).collect(Collectors.toList()));
	}
}
