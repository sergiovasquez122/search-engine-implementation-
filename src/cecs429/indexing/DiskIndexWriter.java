package cecs429.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.List;

public class DiskIndexWriter {
    private RandomAccessFile weightsFile;
    private RandomAccessFile randomAccessFile;
    private String path;

    public DiskIndexWriter(String path ) throws FileNotFoundException {
        this.path=path;
        weightsFile=new RandomAccessFile(path+"\\docWeights.bin", "rw");
        randomAccessFile=new RandomAccessFile(path+"\\posting.bin", "rw");
    }

    public void setWeightsFile(int docId, double weight) throws IOException {
        weightsFile.writeDouble(weight);
    }

    public void close() throws IOException {
        weightsFile.close();
        randomAccessFile.close();
    }

    public void writeIndex(PositionalInvertedIndex index) throws IOException, SQLException {
        List<String> vocabulary = index.getVocabulary();
        Connection connection = DriverManager.getConnection("jdbc:sqlite:"+path+"\\"+"terms.sqlite");
        Statement s = connection.createStatement();
        s.setQueryTimeout(30);
        s.executeUpdate("drop table if exists terms");
        s.executeUpdate("create table terms (term string, pos integer)");

        PreparedStatement statement = connection.prepareStatement("insert into terms values(?,?) on conflict do nothing");
        statement.setQueryTimeout(30);  // set timeout to 30 sec.


        int bytepos = 0;
        for (String term : vocabulary){
            statement.setString(1, term);
            statement.setInt(2, (int) randomAccessFile.getFilePointer());
            statement.executeUpdate();
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
                randomAccessFile.writeInt(posting.getPos().size());
                for (int position : posting.getPos()){
                    newBytePos+=4;
                    randomAccessFile.writeInt(position-lastPos);
                    lastPos=position;
                }
            }

            bytepos=newBytePos;
        }

        connection.close();
    }
}
