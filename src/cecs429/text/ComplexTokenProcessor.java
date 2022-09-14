package cecs429.text;

import java.util.List;

public class ComplexTokenProcessor implements TokenProcessor {
    @Override
    public List<String> processToken(String token) {
        token = token.replaceAll("(^[\\W_])|([\\W_]*$)","");
        return null;
    }
}
