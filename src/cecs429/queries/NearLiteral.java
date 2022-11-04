package cecs429.queries;

import cecs429.indexing.Index;
import cecs429.indexing.Posting;
import cecs429.text.ComplexTokenProcessor;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class NearLiteral implements QueryComponent{
    private List<String> mTerms=new ArrayList<>();
    public NearLiteral(String terms) {
        mTerms.addAll(Arrays.asList(terms.split(" ")));
    }

    @Override
    public List<Posting> getPostings(Index index) throws ClassNotFoundException, InstantiationException, IllegalAccessException, SQLException, IOException {
        ComplexTokenProcessor processor = new ComplexTokenProcessor();
        List<String> s1=processor.processToken(mTerms.get(0));
        List<String> s2=processor.processToken(mTerms.get(2));
        String token = mTerms.get(1);
        int k = Integer.parseInt(token.substring(token.indexOf("/")+1));
        List<Posting> p1=index.getPostings(s1.get(s1.size()-1));
        List<Posting> p2=index.getPostings(s2.get(s2.size()-1));

        return PhraseLiteral.PositionalIntersect(p1,p2,k);
    }

    private boolean sign = false;
    @Override
    public boolean getSign() {
        return sign;
    }

    @Override
    public void setSign(boolean sign) {
        this.sign=sign;
    }
}
