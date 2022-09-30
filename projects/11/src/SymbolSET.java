import java.util.ArrayDeque;
import java.util.Iterator;
import java.util.Queue;
import java.util.Scanner;


public class SymbolSET implements Iterable<String> {
  private static final int R = 256;        // extended ASCII

  private Node root;      // root of trie
  private int n;          // number of keys in trie

  // R-way trie node
  private static class Node {
    private Node[] next = new Node[R];
    private boolean isString;
  }

  /**
   * Initializes an empty set of strings.
   */
  public SymbolSET() {
  }

  /**
   * Does the set contain the given key?
   *
   * @param key the key
   * @return {@code true} if the set contains {@code key} and
   * {@code false} otherwise
   * @throws IllegalArgumentException if {@code key} is {@code null}
   */
  public boolean contains(String key) {
    if (key == null) throw new IllegalArgumentException("argument to contains() is null");
    Node x = get(root, key, 0);
    if (x == null) return false;
    return x.isString;
  }

  private Node get(Node x, String key, int d) {
    if (x == null) return null;
    if (d == key.length()) return x;
    char c = key.charAt(d);
    return get(x.next[c], key, d + 1);
  }

  /**
   * Adds the key to the set if it is not already present.
   *
   * @param key the key to add
   * @throws IllegalArgumentException if {@code key} is {@code null}
   */
  public void add(String key) {
    if (key == null) throw new IllegalArgumentException("argument to add() is null");
    root = add(root, key, 0);
  }

  private Node add(Node x, String key, int d) {
    if (x == null) x = new Node();
    if (d == key.length()) {
      if (!x.isString) n++;
      x.isString = true;
    } else {
      char c = key.charAt(d);
      x.next[c] = add(x.next[c], key, d + 1);
    }
    return x;
  }

  /**
   * Returns the number of strings in the set.
   *
   * @return the number of strings in the set
   */
  public int size() {
    return n;
  }

  /**
   * Is the set empty?
   *
   * @return {@code true} if the set is empty, and {@code false} otherwise
   */
  public boolean isEmpty() {
    return size() == 0;
  }

  /**
   * Returns all of the keys in the set, as an iterator.
   * To iterate over all of the keys in a set named {@code set}, use the
   * foreach notation: {@code for (Key key : set)}.
   *
   * @return an iterator to all of the keys in the set
   */
  public Iterator<String> iterator() {
    return keysWithPrefix("").iterator();
  }

  /**
   * Returns all of the keys in the set that start with {@code prefix}.
   *
   * @param prefix the prefix
   * @return all of the keys in the set that start with {@code prefix},
   * as an iterable
   */
  public Iterable<String> keysWithPrefix(String prefix) {
    Queue<String> results = new ArrayDeque<String>();
    Node x = get(root, prefix, 0);
    collect(x, new StringBuilder(prefix), results);
    return results;
  }

  private void collect(Node x, StringBuilder prefix, Queue<String> results) {
    if (x == null) return;
    if (x.isString) results.add(prefix.toString());
    for (char c = 0; c < R; c++) {
      prefix.append(c);
      collect(x.next[c], prefix, results);
      prefix.deleteCharAt(prefix.length() - 1);
    }
  }

  /**
   * Returns all of the keys in the set that match {@code pattern},
   * where the character '.' is interpreted as a wildcard character.
   *
   * @param pattern the pattern
   * @return all of the keys in the set that match {@code pattern},
   * as an iterable, where . is treated as a wildcard character.
   */
  public Iterable<String> keysThatMatch(String pattern) {
    Queue<String> results = new ArrayDeque<String>();
    StringBuilder prefix = new StringBuilder();
    collect(root, prefix, pattern, results);
    return results;
  }

  private void collect(Node x, StringBuilder prefix, String pattern, Queue<String> results) {
    if (x == null) return;
    int d = prefix.length();
    if (d == pattern.length() && x.isString)
      results.add(prefix.toString());
    if (d == pattern.length())
      return;
    char c = pattern.charAt(d);
    if (c == '.') {
      for (char ch = 0; ch < R; ch++) {
        prefix.append(ch);
        collect(x.next[ch], prefix, pattern, results);
        prefix.deleteCharAt(prefix.length() - 1);
      }
    } else {
      prefix.append(c);
      collect(x.next[c], prefix, pattern, results);
      prefix.deleteCharAt(prefix.length() - 1);
    }
  }

  /**
   * Returns the string in the set that is the longest prefix of {@code query},
   * or {@code null}, if no such string.
   *
   * @param query the query string
   * @return the string in the set that is the longest prefix of {@code query},
   * or {@code null} if no such string
   * @throws IllegalArgumentException if {@code query} is {@code null}
   */
  public String longestPrefixOf(String query) {
    if (query == null) throw new IllegalArgumentException("argument to longestPrefixOf() is null");
    int length = longestPrefixOf(root, query, 0, -1);
    if (length == -1) return null;
    return query.substring(0, length);
  }

  // returns the length of the longest string key in the subtrie
  // rooted at x that is a prefix of the query string,
  // assuming the first d character match and we have already
  // found a prefix match of length length
  private int longestPrefixOf(Node x, String query, int d, int length) {
    if (x == null) return length;
    if (x.isString) length = d;
    if (d == query.length()) return length;
    char c = query.charAt(d);
    return longestPrefixOf(x.next[c], query, d + 1, length);
  }

  /**
   * Removes the key from the set if the key is present.
   *
   * @param key the key
   * @throws IllegalArgumentException if {@code key} is {@code null}
   */
  public void delete(String key) {
    if (key == null) throw new IllegalArgumentException("argument to delete() is null");
    root = delete(root, key, 0);
  }

  private Node delete(Node x, String key, int d) {
    if (x == null) return null;
    if (d == key.length()) {
      if (x.isString) n--;
      x.isString = false;
    } else {
      char c = key.charAt(d);
      x.next[c] = delete(x.next[c], key, d + 1);
    }

    // remove subtrie rooted at x if it is completely empty
    if (x.isString) return x;
    for (int c = 0; c < R; c++)
      if (x.next[c] != null)
        return x;
    return null;
  }


  /**
   * Unit tests the {@code SymbolSET} data type.
   *
   * @param args the command-line arguments
   */
  public static void main(String[] args) {
    SymbolSET keywordSet = new SymbolSET();
    keywordSet.add("class");
    keywordSet.add("constructor");
    keywordSet.add("function");
    keywordSet.add("method");
    keywordSet.add("field");
    keywordSet.add("static");
    keywordSet.add("var");
    keywordSet.add("int");
    keywordSet.add("char");
    keywordSet.add("boolean");
    keywordSet.add("void");
    keywordSet.add("true");
    keywordSet.add("false");
    keywordSet.add("null");
    keywordSet.add("this");
    keywordSet.add("let");
    keywordSet.add("do");
    keywordSet.add("if");
    keywordSet.add("else");
    keywordSet.add("while");
    keywordSet.add("return");

    System.out.println(keywordSet.contains("class"));
  }

  public static void test(String[] args) {
    Scanner scanner = new Scanner(new java.io.BufferedInputStream(System.in), "UTF-8");
    SymbolSET set = new SymbolSET();
    while (scanner.hasNext()) {
      String key = scanner.next();
      set.add(key);
    }

    // print results
    if (set.size() < 100) {
      System.out.println("keys(\"\"):");
      for (String key : set) {
        System.out.println(key);
      }
      System.out.println();
    }

    System.out.println("longestPrefixOf(\"shellsort\"):");
    System.out.println(set.longestPrefixOf("shellsort"));
    System.out.println();

    System.out.println("longestPrefixOf(\"xshellsort\"):");
    System.out.println(set.longestPrefixOf("xshellsort"));
    System.out.println();

    System.out.println("keysWithPrefix(\"shor\"):");
    for (String s : set.keysWithPrefix("shor"))
      System.out.println(s);
    System.out.println();

    System.out.println("keysWithPrefix(\"shortening\"):");
    for (String s : set.keysWithPrefix("shortening"))
      System.out.println(s);
    System.out.println();

    System.out.println("keysThatMatch(\".he.l.\"):");
    for (String s : set.keysThatMatch(".he.l."))
      System.out.println(s);
  }
}

