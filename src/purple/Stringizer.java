package purple;

/**
 * Detokenizes a tokenstream into a string.
 *
 * Useful for debugging.
 *
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
class Stringizer {
  public String detokenize(Token[] tokens) {
    StringBuilder builder = new StringBuilder();
    for (Token token : tokens) {
      String out;

      switch (token.getKind()) {
        case GROUPING_LPAREN:
          out = "{{";
          break;
        case GROUPING_RPAREN:
          out = "}}";
          break;
        default:
          out = token.getName();
      }

      builder.append(out);
    }

    return builder.toString();
  }
}
