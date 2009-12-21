package purple;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public enum TokenKind {
  IDENT, DOT, TYPE_IDENT,

  /** built in types/literals **/
  INTEGER, DECIMAL, STRING, REGEX,

  /** balancers **/
  LBRACE, RBRACE,
  LPAREN, RPAREN,
  GROUPING_LPAREN, GROUPING_RPAREN,
  LBRACKET, RBRACKET,

  /** separators **/
  COMMA,
  COLON,
  THIN_ARROW,
  FAT_ARROW,
  EOL,

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

    // Only type names are allowed to begin with upper case.
    if (Character.isUpperCase(st.charAt(0))) {
      return TYPE_IDENT; 
    }

    // TODO validate ident or return null
    return IDENT;
  }
}
