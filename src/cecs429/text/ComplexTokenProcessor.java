package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ComplexTokenProcessor implements TokenProcessor {
    @Override
    public List<String> processToken(String token) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<String> result = new ArrayList<>();
        token = token.replaceAll("(^[\\W_])|([\\W_]*$)","");
        token = token.replaceAll("\"'","");
        if (token.contains("-")){
               String[] tokens = token.toLowerCase().split("-");
               token = token.replaceAll("-","").toLowerCase();
               List<String> strings= new ArrayList<>(Arrays.asList(tokens));
               strings.add(token);
            Class stemClass = Class.forName("org.tartarus.snowball.ext." + "englishStemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
for (String word : strings){
    stemmer.setCurrent(word);
    stemmer.stem();
    result.add(stemmer.getCurrent());
}
        } else {
            token = token.toLowerCase();
            Class stemClass = Class.forName("org.tartarus.snowball.ext." + "englishStemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
            stemmer.setCurrent(token);
            stemmer.stem();
           result.add(stemmer.getCurrent());
        }
        return result;
    }

    public static void main(String[] args) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        TokenProcessor processor = new ComplexTokenProcessor();
        for (String word : processor.processToken("H-P-L")){
            System.out.println(word);

        }
    }
}
