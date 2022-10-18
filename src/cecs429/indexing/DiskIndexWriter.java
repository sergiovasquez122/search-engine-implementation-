package cecs429.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.util.List;

public class DiskIndexWriter {
    public void writeIndex(PositionalInvertedIndex index, String absoluteFilePath) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFilePath,"rw");
        List<String> vocabulary = index.getVocabulary();
        for (String term : vocabulary){
            List<Posting> postings = index.getPostings(term);
            // dft
            randomAccessFile.writeInt(postings.size());
            int lastID = 0;
            for (Posting posting : postings){
                randomAccessFile.writeInt(posting.getDocumentId() - lastID);
                lastID = posting.getDocumentId();

                int lastPos = 0;
                randomAccessFile.writeInt(posting.getTftd());
                for (int position : posting.getPos()){
                    randomAccessFile.writeInt(position-lastPos);
                    lastPos=position;
                }
            }
        }
    }
}
