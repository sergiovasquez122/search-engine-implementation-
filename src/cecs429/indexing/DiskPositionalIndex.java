package cecs429.indexing;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

public class DiskPositionalIndex implements Index{

    @Override
    public List<Posting> getPostings(String term) throws IOException {
        List<Posting> result = new ArrayList<>();
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
    public List<Posting> getPostingsWithoutPos(String term) throws IOException {
        List<Posting> result = new ArrayList<>();
        int dft = randomAccessFile.readInt();
        int gap=0;
        for (int i = 0;i<dft;i++){
            int id=randomAccessFile.readInt();
            Posting posting = new Posting(id-gap);
            gap=id;
            int tftd = randomAccessFile.readInt();
            randomAccessFile.skipBytes(tftd*4);
            result.add(posting);
        }
        return result;
    }

    @Override
    public List<String> getVocabulary() {
        return null;
    }

    public DiskPositionalIndex(String absoluteFilePath) throws FileNotFoundException {
        randomAccessFile = new RandomAccessFile(absoluteFilePath,"r");
    }

    private RandomAccessFile randomAccessFile;
}
