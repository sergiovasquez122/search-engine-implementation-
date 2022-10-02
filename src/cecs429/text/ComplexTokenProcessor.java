package cecs429.text;

import org.tartarus.snowball.SnowballStemmer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ComplexTokenProcessor implements TokenProcessor {
    private Pattern removeNonAlpha=Pattern.compile("^[\\W_]|[\\W_]$");

    public static void replaceAll(StringBuilder builder, String from, String to) {
        int index = builder.indexOf(from);
        while (index != -1) {
            builder.replace(index, index + from.length(), to);
            index += to.length(); // Move to the end of the replacement
            index = builder.indexOf(from, index);
        }
    }

    public static void toLowerCase(StringBuilder builder) {
        for (int i = 0; i < builder.length(); i++) {
            if (Character.isUpperCase(builder.charAt(i))) {
                builder.setCharAt(i,
                        Character.toLowerCase(builder.charAt(i)));
            }
        }
    }

    @Override
    public List<String> processToken(String token) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        List<String> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        Matcher m = removeNonAlpha.matcher(token);
        while (m.find()){
            m.appendReplacement(stringBuilder,"");
        }
        m.appendTail(stringBuilder);
        replaceAll(stringBuilder,"\"","");
        replaceAll(stringBuilder,"'","");
        if (token.contains("-")){
               String[] tokens = stringBuilder.toString().toLowerCase().split("-");
               replaceAll(stringBuilder,"-","");
               toLowerCase(stringBuilder);
               List<String> strings= new ArrayList<>(Arrays.asList(tokens));
               strings.add(stringBuilder.toString());
            Class stemClass = Class.forName("org.tartarus.snowball.ext." + "englishStemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
for (String word : strings){
    stemmer.setCurrent(word);
    stemmer.stem();
    result.add(stemmer.getCurrent());
}

        } else {
            toLowerCase(stringBuilder);
            Class stemClass = Class.forName("org.tartarus.snowball.ext." + "englishStemmer");
            SnowballStemmer stemmer = (SnowballStemmer) stemClass.newInstance();
            stemmer.setCurrent(stringBuilder.toString());
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
