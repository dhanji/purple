package purple;

import java.util.List;

/**
 * Detokenizes a tokenstream into a string.
 *
 * Useful for debugging.
 *
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
public class Stringizer {
  public String detokenize(List<Token> tokens) {
    return detokenize(tokens.toArray(new Token[tokens.size()]));
  }

  public String detokenize(Token[] tokens) {
    StringBuilder builder = new StringBuilder();
    for (Token token : tokens) {
      String out;

      switch (token.getKind()) {
        case GROUPING_LPAREN:
          out = "<<";
          break;
        case GROUPING_RPAREN:
          out = ">>";
          break;
        default:
          out = token.getPrintableName();
      }

      builder.append(out);
    }

    return builder.toString();
  }

  public static String tokenizeAndStringify(String script) {
    return new Stringizer().detokenize(new Tokenizer(script).tokenize());
  }
}
