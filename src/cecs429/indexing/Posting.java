package cecs429.indexing;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;
	private List<Integer> mPos =new ArrayList<>();

	public List<Integer> getPos() {
		return mPos;
	}

	public void addPos(int pos){mPos.add(pos);}

	public Posting(int documentId) {
		mDocumentId = documentId;
	}

	public Posting(int documentId, int pos){
		mDocumentId=documentId;
		mPos.add(pos);
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
}
