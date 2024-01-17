
import java.util.Iterator;

/**
 * NGramIterator class creates the ngram tokens of the provided string
 *
 * @author PC
 */
public class NGramIterator implements Iterator<String> {

    private final String str;
    private final int n;
    int pos = 0;

    /**
     *
     * @param n
     * @param str
     */
    public NGramIterator(int n, String str) {
        this.n = n;
        this.str = str;
    }

    /**
     *
     * @return
     */
    public boolean hasNext() {
        return pos < str.length() - n + 1;
    }

    /**
     *
     * @return
     */
    public String next() {
        return str.substring(pos, pos++ + n);
    }
}
