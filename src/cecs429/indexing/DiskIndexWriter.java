package cecs429.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

public class DiskIndexWriter {
    public void writeIndex(PositionalInvertedIndex index, String absoluteFilePath) throws IOException, SQLException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(absoluteFilePath,"rw");
        List<String> vocabulary = index.getVocabulary();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:terms.sqlite");
        Statement statement = connection.createStatement();
        statement.setQueryTimeout(30);  // set timeout to 30 sec.

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
                statement.executeUpdate("insert into terms values(term,1)");
            }
        }
    }
}
