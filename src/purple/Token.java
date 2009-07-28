package purple;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Token {
  private final String name;
  private final TokenKind tokenKind;

  public Token(String name, TokenKind tokenKind) {
    this.name = name;
    this.tokenKind = tokenKind;
  }

  @Override
  public String toString() {
    return new StringBuilder().append("Token{")
        .append("name='")
        .append(name)
        .append('\'')
        .append(", tokenKind=")
        .append(tokenKind)
        .append('}')
        .toString();
  }

  public TokenKind getKind() {
    return tokenKind;
  }

  public String getName() {
    return name;
  }
}
