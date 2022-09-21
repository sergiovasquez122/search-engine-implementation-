package cecs429.queries;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;

/**
 * Represents a phrase literal consisting of one or more terms that must occur in sequence.
 */
public class PhraseLiteral implements QueryComponent {
	// The list of individual terms in the phrase.
	private List<String> mTerms = new ArrayList<>();
	
	/**
	 * Constructs a PhraseLiteral with the given individual phrase terms.
	 */
	public PhraseLiteral(List<String> terms) {
		mTerms.addAll(terms);
	}
	
	/**
	 * Constructs a PhraseLiteral given a string with one or more individual terms separated by spaces.
	 */
	public PhraseLiteral(String terms) {
		mTerms.addAll(Arrays.asList(terms.split(" ")));
	}
	
	@Override
	public List<Posting> getPostings(Index index) {
		List<Posting> result = null;
		for (int k=1;k<= mTerms.size()-1;k++){
			List<Posting> p1 = index.getPostings(mTerms.get(k-1));
			List<Posting> p2 = index.getPostings(mTerms.get(k));
			result=PositionalIntersect(p1,p2,k);
		}
		return result;

	}
	private static void intersectHelper (Posting p1, Posting p2,int k, List<Posting> answer ){

		List<Integer> l = new ArrayList<>();
		List<Integer> pp1 =p1.getPos();
		List<Integer> pp2 =p2.getPos();
		int pp1idx=0;
		int pp2idx=0;
		while (pp1idx<pp1.size()){
			while (pp2idx<pp2.size()){
				if (Math.abs(pp1.get(pp1idx) - pp2.get(pp2idx)) <= k){
					l.add(pp2.get(pp2idx));
				}
				else if ((pp2.get(pp2idx))>pp1.get(pp1idx)){
					break;
				}
				pp2idx++;
			}
			while ((!l.isEmpty()) && Math.abs(l.get(0)-pp2.get(pp2idx))>k){
				l.remove(0);
			}
			for (Integer pos : l){
				if (!answer.isEmpty() && answer.get(answer.size()-1).getDocumentId()==p1.getDocumentId()){
					answer.get(answer.size()-1).addPos(pp1idx);
				}else {
					answer.add(new Posting(p1.getDocumentId(),pp1idx));
				}
			}
			pp1idx++;
		}
	}

	private static List<Posting> PositionalIntersect(List<Posting> p1,List<Posting> p2, int k){
		List<Posting> answer = new ArrayList<>();
		int l1_idx =0;
		int l2_idx =0;
		while (l1_idx<p1.size()&&l2_idx<p2.size()){
			Posting posting1 = p1.get(l1_idx);
			Posting posting2 = p2.get(l2_idx);
			if (posting1.getDocumentId()==posting2.getDocumentId()){
				l1_idx++;
				l2_idx++;
			}else if (posting1.getDocumentId()<posting2.getDocumentId()){
				l1_idx++;
				l2_idx++;
			}
		}
		return answer;
	}
	@Override
	public String toString() {
		String terms = 
			mTerms.stream()
			.collect(Collectors.joining(" "));
		return "\"" + terms + "\"";
	}
}
