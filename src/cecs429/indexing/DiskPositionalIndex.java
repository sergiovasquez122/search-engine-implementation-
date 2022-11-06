package cecs429.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index{

    private Connection connection;
    @Override
    public List<Posting> getPostings(String term) throws IOException, SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from terms where term=?");
        statement.setString(1, term);
        List<Posting> result = new ArrayList<>();
        ResultSet resultSet= statement.executeQuery();
        if (!resultSet.next()){
            return result;
        }
        int bytepos = resultSet.getInt("pos");
        randomAccessFile.seek(bytepos);
        int dft = randomAccessFile.readInt();
        int gap=0;
        for (int i = 0;i<dft;i++){
            int id=randomAccessFile.readInt();
            Posting posting = new Posting(id+gap);
            gap+=id;
            int tftd = randomAccessFile.readInt();
            int posgap=0;

            for (int j=0;j<tftd;j++){
                int pos=randomAccessFile.readInt();
                posting.addPos(pos+posgap);
                posgap+=pos;
            }
            posting.setTftd(tftd);
           result.add(posting);
        }
        return result;
    }

    @Override
    public List<Posting> getPostingsWithoutPos(String term) throws IOException, SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from terms where term=?");
        statement.setQueryTimeout(30);  // set timeout to 30 sec.
        statement.setString(1, term);
        List<Posting> result = new ArrayList<>();
        ResultSet resultSet= statement.executeQuery();
        if (!resultSet.next()){
            return result;
        }
        int pos = resultSet.getInt("pos");
        randomAccessFile.seek(pos);
        int dft = randomAccessFile.readInt();
        int gap=0;
        for (int i = 0;i<dft;i++){
            int id=randomAccessFile.readInt();
            Posting posting = new Posting(id+gap);
            gap+=id;
            int tftd = randomAccessFile.readInt();
            for (int j=0;j<tftd;j++){
                randomAccessFile.readInt();
            }
            posting.setTftd(tftd);
            result.add(posting);
        }
        return result;
    }

    public double getEuclideanWeight(int docID) throws IOException {
        weights.seek(docID* 8L);
        return weights.readDouble();
    }

    public double avgTftd(int docID) throws IOException {
        weights.seek(docID* 8L);
        return weights.readDouble();
    }

    public double getDocLength(int docID) throws IOException {
        weights.seek(docID* 8L);
        return weights.readDouble();
    }

    public double getAvgDocLength() throws IOException {
        return weights.readDouble();
    }

    public double byteSize(int d) throws IOException {
        return weights.readDouble();
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }

    public DiskPositionalIndex(String absoluteDirectory) throws FileNotFoundException, SQLException {
        randomAccessFile = new RandomAccessFile(absoluteDirectory+"\\posting.bin","r");
        weights = new RandomAccessFile(absoluteDirectory+"\\docWeights.bin","r");
        connection = DriverManager.getConnection("jdbc:sqlite:terms.sqlite");
    }

    private RandomAccessFile randomAccessFile;
    private RandomAccessFile weights;
}
