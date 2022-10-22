package cecs429.queries;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.ComplexTokenProcessor;

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
	public List<Posting> getPostings(Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, IOException, SQLException {
		ComplexTokenProcessor processor = new ComplexTokenProcessor();
		List<String> s1=processor.processToken(mTerms.get(0));
		List<Posting> result = new ArrayList<>(index.getPostings(s1.get(s1.size() - 1)));
		for (int k=1;k<= mTerms.size()-1;k++){
			List<String> s2=processor.processToken(mTerms.get(k));
			List<Posting> p2 = index.getPostings(s2.get(s2.size()-1));
			result=PositionalIntersect(result,p2,k);
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
			while ((!l.isEmpty()) && Math.abs(l.get(0)-pp1.get(pp1idx))>k){
				l.remove(0);
			}
			for (Integer pos : l){
				if (!answer.isEmpty() && answer.get(answer.size()-1).getDocumentId()==p1.getDocumentId()){
					answer.get(answer.size()-1).addPos(pp1.get(pp1idx));
				}else {
					answer.add(new Posting(p1.getDocumentId(),pp1.get(pp1idx)));
				}
			}
			pp1idx++;
		}
	}

	public static List<Posting> PositionalIntersect(List<Posting> p1,List<Posting> p2, int k){
		List<Posting> answer = new ArrayList<>();
		int l1_idx =0;
		int l2_idx =0;
		while (l1_idx<p1.size()&&l2_idx<p2.size()){
			Posting posting1 = p1.get(l1_idx);
			Posting posting2 = p2.get(l2_idx);
			if (posting1.getDocumentId()==posting2.getDocumentId()){
				intersectHelper(posting1,posting2,k,answer);
				l1_idx++;
				l2_idx++;
			}else if (posting1.getDocumentId()<posting2.getDocumentId()){
				l1_idx++;
			}else {
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
