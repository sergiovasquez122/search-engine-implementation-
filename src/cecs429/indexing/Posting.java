package cecs429.indexing;

import java.util.ArrayList;
import java.util.List;

/**
 * A Posting encapulates a document ID associated with a search query component.
 */
public class Posting {
	private int mDocumentId;

	private int tftd = 0;

	private List<Integer> mPos =new ArrayList<>();

	public List<Integer> getPos() {
		return mPos;
	}

	public void addPos(int pos){
		mPos.add(pos);
		tftd++;
	}

	public Posting(int documentId) {
		mDocumentId = documentId;
	}

	public Posting(int documentId, int pos){
		mDocumentId=documentId;
		addPos(pos);
	}

	public int getTftd(){
		return tftd;
	}
	
	public int getDocumentId() {
		return mDocumentId;
	}
}
