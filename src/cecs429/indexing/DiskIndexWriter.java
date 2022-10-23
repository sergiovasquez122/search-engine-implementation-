package cecs429.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class DiskIndexWriter {
    private RandomAccessFile weightsFile = new RandomAccessFile(Paths.get("").toAbsolutePath().toString()+"\\weights.bin", "rw");
    private RandomAccessFile randomAccessFile = new RandomAccessFile(Paths.get("").toAbsolutePath().toString()+"\\post.bin","rw");

    public DiskIndexWriter() throws FileNotFoundException {
    }

    public void setWeightsFile(int docId, double weight) throws IOException {
        weightsFile.seek((docId-1)* 8L);
        weightsFile.writeDouble(weight);
    }

    public void close() throws IOException {
        weightsFile.close();
        randomAccessFile.close();
    }

    public void writeIndex(PositionalInvertedIndex index) throws IOException, SQLException {
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

        connection.close();
    }
}
