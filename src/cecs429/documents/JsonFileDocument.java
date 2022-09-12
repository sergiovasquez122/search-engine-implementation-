package cecs429.documents;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class JsonFileDocument implements FileDocument{

    private int mDocumentId;
    private Path mFilePath;
    String title;

    /**
     * Constructs a TextFileDocument with the given document ID representing the file at the given
     * absolute file path.
     */
    public JsonFileDocument(int id, Path absoluteFilePath) {
        mDocumentId = id;
        mFilePath = absoluteFilePath;
        try {
            BufferedReader reader = Files.newBufferedReader(mFilePath);
            Gson gson =  new Gson();
            Type mapType = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> map = gson.fromJson(reader, mapType);
            title = map.get("title");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public int getId() {
        return mDocumentId;
    }

    @Override
    public Reader getContent() {
        try {
            BufferedReader reader = Files.newBufferedReader(mFilePath);
            Gson gson =  new Gson();
            Type mapType = new TypeToken<Map<String, String>>(){}.getType();
            Map<String, String> map = gson.fromJson(reader, mapType);
            return new StringReader(map.get("body"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public Path getFilePath() {
        return mFilePath;
    }
    public static FileDocument loadJsonFileDocument(Path absolutePath, int documentId) {
        return new JsonFileDocument(documentId, absolutePath);
    }
}
