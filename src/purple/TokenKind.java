package purple;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public enum TokenKind {
  IDENT, DOT,

  /** built in types/literals **/
  INTEGER, DECIMAL, STRING,

  /** balancers **/
  LBRACE, RBRACE,
  LPAREN, RPAREN,
  LBRACKET, RBRACKET,

  /** separators **/
  COMMA,
  COLON,
  THIN_ARROW,
  FAT_ARROW,

  /** Keywords **/
  MODULE,
  REQUIRE,
  DEF,
  CLASS;

  public static TokenKind resolve(String st) {
    TokenKind tokenKind = Keywords.get(st);
    if (null != tokenKind) {
      return tokenKind;
    }

    // Add other resolutions here.
    // TODO chance for optimization/interning
    if ("->".equals(st)) {
      return THIN_ARROW;
    } else if ("=>".equals(st)) {
      return FAT_ARROW;
    }

    // TODO validate ident or return null
    return IDENT;
  }
}
