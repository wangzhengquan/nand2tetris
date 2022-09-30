public class XMLUtil {
  private static SymbolMap<String> escapeCharMap = new SymbolMap<>();

  static {
    escapeCharMap.put("<", "&lt;");
    escapeCharMap.put(">", "&gt;");
    escapeCharMap.put("\"", "&quot;");
    escapeCharMap.put("&", "&amp;");
  }

  public static String escape(String token) {
    if (escapeCharMap.contains(token))
      return escapeCharMap.get(token);
    return token;
  }

  public static void main(String[] args) {
  }
}
