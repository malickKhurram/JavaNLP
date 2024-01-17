
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 *
 * @author PC
 */
public class NLPProcessing {

    private String localFolder;
    private String mysteryFile = "mystery.txt";
    private int ngram = 2;
    private String fileExtensionToRead = ".txt";
    private Map<String, List<String>> languageModel = new HashMap<>();
    private Map<String, Map<String, Integer>> languageModelCount = new HashMap<>();
    private List<String> languages;
    ExecutorService threadPool = Executors.newCachedThreadPool();
    double sqrtRootProduct = 0.0;
    double AdotB = 0.0;
    /**
     *
     * @param localFolder
     */
    public NLPProcessing(String localFolder, int ngram) {
        this.localFolder = localFolder;
        this.ngram = ngram;
    }

    public void loadFilesAndConstructModel() {
        try {
            NGramModelConstruct.nLPProcessing = this;//All threads will have same reference
            languages = listFolders(localFolder);
            languages.forEach(System.out::println);
            languages.stream().forEach(language -> {
                try {
                    System.out.println("\n===> List of Files for:" + language);
                    List<String> files = listFiles(language, fileExtensionToRead);
                    files.forEach(System.out::println);
                    files.forEach((file -> {
                        NGramModelConstruct modelConstruct = new NGramModelConstruct(language, file, this.ngram);
                        threadPool.execute(modelConstruct);
                    }));

                    NGramModelConstruct modelConstruct = new NGramModelConstruct(mysteryFile, localFolder + "\\" + mysteryFile, this.ngram);
                    threadPool.execute(modelConstruct);

                } catch (IOException ex) {
                    Logger.getLogger(NLPProcessing.class.getName()).log(Level.SEVERE, null, ex);
                }
            });

        } catch (IOException ex) {
            Logger.getLogger(NLPProcessing.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Synchronized method where all the threads add the ngrams
     *
     * @param folder
     * @param ngram
     */
    protected synchronized void addNGrams(String language, List<String> ngram) {

        if (languageModel.get(language) == null) {
            List<String> ngrams = new ArrayList<>();
            ngrams.addAll(ngram);
            languageModel.put(language, ngrams);
        } else {
            List<String> ngrams = languageModel.get(language);
            ngrams.addAll(ngram);
            languageModel.put(language, ngrams);
        }
    }

    /**
     * prints the constructed
     */
    public void printConstructedLanguageModel() {
        System.out.println("\n======================================");
        languageModel.forEach(
                (key, value)
                -> {
                    System.out.println(key + " =: " + value.size());
                });
        System.out.println();
    }
    
    /**
     * prints the constructed
     */
    public void printConstructedLanguageModelWithCount() {
        System.out.println("\n======================================");
        languageModelCount.forEach(
                (key, value)
                -> {
                    System.out.println(key + " =: " + value);
                });
        System.out.println();
    }
    

    /**
     * Calculates the similarity distance
     */

    public void calculateDocumentDistance() {

        Map<String, Double> similarity = new HashMap<String, Double>();
        
        languageModel.forEach((key, value) -> {
            Map<String, Integer> ngramCount = new HashMap<String, Integer>();
            value.stream().forEach(ngram -> {
                Integer count = ngramCount.get(ngram);
                ngramCount.put(ngram, (count == null) ? 1 : count + 1);
            });
            languageModelCount.put(key, ngramCount);
        });

//        printConstructedLanguageModelWithCount();
        
        double ASqrtProduct = calculateSqrtProduct(mysteryFile, languageModelCount.get(mysteryFile));//Calculate |A|
        languages.stream().forEach(language -> {
            AdotB = 0.0;
            double LSqrtProduct = calculateSqrtProduct(language, languageModelCount.get(language));//Calculate |B|
            languageModelCount.get(mysteryFile).forEach((key, value) -> {// Calculate A.B where A is the mystery file
                if (languageModelCount.get(language).get(key) != null) {
                    AdotB += value * languageModelCount.get(language).get(key);
                }
            });
            
            double AB = ASqrtProduct*LSqrtProduct;
            double textSimilarity = AdotB/AB;
            System.out.println(String.format("Text Similarity with " + language + " => %,.2f", textSimilarity));
            similarity.put(language, textSimilarity);
        });
    }

    public double calculateSqrtProduct(String language, Map<String, Integer> ngramCount) {
        ngramCount.values().forEach(value -> {
            sqrtRootProduct += Math.pow(value, 2);
        });
        sqrtRootProduct = Math.sqrt(sqrtRootProduct);
        return sqrtRootProduct;
    }

    /**
     * Extracts the list of files in a language folder
     *
     * @param path
     * @param ext
     * @return
     * @throws IOException
     */
    public List<String> listFiles(String path, String ext) throws IOException {
        Path start = Paths.get(path);
        try (Stream<Path> stream = Files.walk(start, 1)) {
            return stream
                    .map(String::valueOf)
                    .filter(file -> file.contains(ext))
                    .sorted()
                    .collect(Collectors.toList());

        }
    }

    /**
     * Extracts the list of language folders
     *
     * @param path
     * @return
     * @throws IOException
     */
    public List<String> listFolders(String path) throws IOException {
        Path start = Paths.get(path);
        try (Stream<Path> stream = Files.walk(start, 1)) {
            return stream
                    .map(String::valueOf)
                    .filter(file -> new File(file).isDirectory() && !file.equalsIgnoreCase(path))
                    .sorted()
                    .collect(Collectors.toList());

        }
    }

    public void startProcessing() {
        loadFilesAndConstructModel();
        ///////////////////////// Wait for all threads to construct the ngrams
        threadPool.shutdown();
        try {
            if (!threadPool.awaitTermination(60, TimeUnit.MINUTES)) {
                threadPool.shutdownNow();
            }
        } catch (InterruptedException ex) {
            threadPool.shutdownNow();
            Thread.currentThread().interrupt();
        }

        printConstructedLanguageModel();
        calculateDocumentDistance();
        
    }

    /**
     *
     * @param args
     */
    public static void main(String args[]) {
        if (args.length != 2) {
            System.err.println("Please provide local folder and n-gram!");
            return;
        }
        String localFolder = args[0];
        int ngram = Integer.parseInt(args[1]);
        NLPProcessing nLPProcessing = new NLPProcessing(localFolder, ngram);
        nLPProcessing.startProcessing();
    }
}
