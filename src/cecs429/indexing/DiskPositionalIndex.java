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
            Posting posting = new Posting(id-gap);
            gap=id;
            int tftd = randomAccessFile.readInt();
            int posgap=0;

            for (int j=0;j<tftd;j++){
                int pos=randomAccessFile.readInt();
                posting.addPos(pos-posgap);
                posgap=pos;
            }
            posting.setTftd(tftd);
           result.add(posting);
        }
        return result;
    }

    @Override
    public List<Posting> getPostingsWithoutPos(String term) throws IOException, SQLException {
        PreparedStatement statement = connection.prepareStatement("select * from terms where term=?");
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
            gap=id;
            int tftd = randomAccessFile.readInt();
            for (int j=0;j<tftd;j++){
                randomAccessFile.readInt();
            }
            result.add(posting);
        }
        return result;
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }

    public DiskPositionalIndex(String absoluteFilePath) throws FileNotFoundException, SQLException {
        randomAccessFile = new RandomAccessFile(absoluteFilePath,"r");
        connection = DriverManager.getConnection("jdbc:sqlite:terms.sqlite");
    }

    private RandomAccessFile randomAccessFile;
}
