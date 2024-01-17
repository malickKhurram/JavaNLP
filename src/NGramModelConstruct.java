
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 *
 * @author PC
 */
public class NGramModelConstruct extends Thread {

    protected static NLPProcessing nLPProcessing;
    private String language;
    private String file;
    private int n;

    public NGramModelConstruct(String language, String file, int n) {
        this.language = language;
        this.file = file;
        this.n = n;
    }

    @Override
    public void run() {
        List<String> ngrams = generateNGramsFromFile(file, " ", true);
        nLPProcessing.addNGrams(language, ngrams);
    }

    public List<String> generateNGramsFromFile(String fileName, String delim, boolean returnDelims) {
        List<String> ngrams = new ArrayList<>();
        String currLine = "";
        StringTokenizer tokenizer;
        try (BufferedReader br = new BufferedReader(
                new FileReader(fileName))) {
            while ((currLine = br.readLine()) != null) {
                tokenizer = new StringTokenizer(currLine, delim, returnDelims);
                while (tokenizer.hasMoreElements()) {
                    String token = tokenizer.nextToken().replaceAll("[^a-zA-Z0-9]", "");
                    token = token.toLowerCase();
                    if (n > 1) {
                        if (token.length() >= n) {
                            new NGramIterator(n, token).forEachRemaining(ngram -> {
                                ngrams.add(ngram);

                            });//Replace all special chars and generate the ngrams from token
                        }
                    } else {
                        ngrams.add(token);
                    }
                }//End of while (tokenizer.hasMoreElements()) {
            }//End of while ((currLine = br.readLine()) != null) { 

        } catch (IOException e) {
            e.printStackTrace();
        }
        return ngrams;
    }
}
