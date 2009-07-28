package purple;

import java.util.HashMap;
import java.util.Map;

/**
 * Tools for working with keywords
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Keywords {

  private static final Map<String, TokenKind> keywords;

  static {
    keywords = new HashMap<String, TokenKind>();
    keywords.put("module", TokenKind.MODULE);
    keywords.put("def", TokenKind.DEF);
    keywords.put("require", TokenKind.REQUIRE);
    keywords.put("class", TokenKind.CLASS);
  }

  public static TokenKind get(String string) {
    return keywords.get(string);
  }
}
