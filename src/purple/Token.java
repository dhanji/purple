package purple;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Token {
  private final String name;
  private final TokenKind tokenKind;

  private int balancingTokenIndex;

  public Token(String name, TokenKind tokenKind) {
    this.name = name;
    this.tokenKind = tokenKind;
  }

  @Override
  public String toString() {
    String name = getPrintableName();
    return new StringBuilder().append("Token{")
        .append("name='")
        .append(name)
        .append('\'')
        .append(", tokenKind=")
        .append(tokenKind)
        .append('}')
        .toString();
  }

  public String getPrintableName() {
    return TokenKind.EOL.equals(this.tokenKind) ? "\\n" : this.name;
  }

  public TokenKind getKind() {
    return tokenKind;
  }

  public String getName() {
    return name;
  }

  public int getBalancingTokenIndex() {
    return balancingTokenIndex;
  }

  public void setBalancingTokenIndex(int balancingTokenIndex) {
    this.balancingTokenIndex = balancingTokenIndex;
  }

  public boolean isInteger() {
    return tokenKind.equals(TokenKind.INTEGER);
  }
  
  public boolean isDot() {
    return tokenKind.equals(TokenKind.DOT);
  }

  public boolean isDef() {
    return TokenKind.DEF.equals(tokenKind);
  }

  public boolean isEol() {
    return TokenKind.EOL.equals(tokenKind);
  }

  public boolean is(TokenKind kind) {
    return kind.equals(tokenKind);
  }

  public boolean isExpressionDelimiter() {
    return TokenKind.EOL.equals(tokenKind)
          || TokenKind.RBRACE.equals(tokenKind)
          || TokenKind.RPAREN.equals(tokenKind)
          || TokenKind.GROUPING_RPAREN.equals(tokenKind);
  }

  // Optimize, these don't need to be null-safe
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    Token token = (Token) o;

    if (name != null ? !name.equals(token.name) : token.name != null) return false;
    if (tokenKind != token.tokenKind) return false;

    return true;
  }

  @Override
  public int hashCode() {
    int result = name != null ? name.hashCode() : 0;
    result = 31 * result + (tokenKind != null ? tokenKind.hashCode() : 0);
    result = 31 * result + balancingTokenIndex;
    return result;
  }

  static Token dot() {
    return new Token(".", TokenKind.DOT);
  }

  static Token lparen() {
    return new Token("(", TokenKind.LPAREN);
  }

  static Token rparen() {
    return new Token(")", TokenKind.RPAREN);
  }
}
