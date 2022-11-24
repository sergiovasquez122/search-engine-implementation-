package cecs429.documents;

import java.nio.file.Path;

/**
 * Represents a collection of documents used to build an index.
 */
public interface DocumentCorpus {
	/**
	 * Gets all documents in the corpus.
	 */
	Iterable<Document> getDocuments();
	
	/**
	 * The number of documents in the corpus.
	 */
	int getCorpusSize();

	String getClass(int id);

	/**
	 * Returns the document with the given document ID.
	 */
	Document getDocument(int id);
	Path getmDirectoryPath();
}
