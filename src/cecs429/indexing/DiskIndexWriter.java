package cecs429.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.sql.*;
import java.util.List;

public class DiskIndexWriter {
    public void writeIndex(PositionalInvertedIndex index, String absoluteFilePath) throws IOException, SQLException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFilePath,"rw");
        List<String> vocabulary = index.getVocabulary();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:terms.sqlite");
        PreparedStatement statement = connection.prepareStatement("insert into terms values(?,?)");
        statement.setQueryTimeout(30);  // set timeout to 30 sec.

        int bytepos = 0;
        for (String term : vocabulary){
            List<Posting> postings = index.getPostings(term);
            // dft
            int newBytePos = bytepos;
            newBytePos+=4;
            randomAccessFile.writeInt(postings.size());
            int lastID = 0;
            for (Posting posting : postings){
                newBytePos+=4;
                randomAccessFile.writeInt(posting.getDocumentId() - lastID);
                lastID = posting.getDocumentId();
                int lastPos = 0;
                newBytePos+=4;
                randomAccessFile.writeInt(posting.getTftd());
                for (int position : posting.getPos()){
                    newBytePos+=4;
                    randomAccessFile.writeInt(position-lastPos);
                    lastPos=position;
                }
            }

            statement.setString(1, term);
            statement.setInt(2, bytepos);
            statement.executeUpdate();

            bytepos=newBytePos;
        }
    }
}
