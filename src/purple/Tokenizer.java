package purple;

import java.util.*;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Tokenizer {
  private final String expression;
  private final char[] expr;

  public Tokenizer(String expression) {
    this.expression = expression;
    this.expr = expression.toCharArray();
  }

  private static final Map<Character, TokenKind> charTokenKinds;

  static {
    charTokenKinds = new HashMap<Character, TokenKind>();
    charTokenKinds.put('.', TokenKind.DOT);
    charTokenKinds.put(',', TokenKind.COMMA);
    charTokenKinds.put(':', TokenKind.COLON);
    charTokenKinds.put('(', TokenKind.LPAREN);
    charTokenKinds.put(')', TokenKind.RPAREN);
    charTokenKinds.put('{', TokenKind.LBRACE);
    charTokenKinds.put('}', TokenKind.RBRACE);
    charTokenKinds.put('[', TokenKind.LBRACKET);
    charTokenKinds.put(']', TokenKind.RBRACKET);
  }

  // parser state machine
  private boolean inToken;
  private boolean isNumeric;
  private List<Token> tokens = new ArrayList<Token>();

  public List<Token> tokenize() {
    inToken = !Character.isWhitespace(expr[0]);
    isNumeric = true;

    StringBuilder token = new StringBuilder();
    for (int i = 0; i < expr.length; i++) {
      char c = expr[i];

      // Add EOLs (and see if the world asplodes)
      if ('\n' == c) {
        tokens.add(new Token("\n", TokenKind.EOL));
      }

      // Whitespace handler
      if (Character.isWhitespace(c)) {

        // end of token?
        if (inToken) {
          token = bakeToken(token);
        }
        
        continue;
      }
      
      // dot and comma separators
      if (charTokenKinds.containsKey(c)) {

        if (token.length() > 0)
          token = bakeToken(token);

        //add period as token too.
        tokens.add(new Token(Character.toString(c), charTokenKinds.get(c)));

        continue;
      }


      // Decide if the current token we're looking at is not a pure integer
      if (!Character.isDigit(c)) {
        isNumeric = false;
      }

      inToken = true;
      token.append(c);
    }

    // last token.
    //noinspection UnusedAssignment
    token = bakeToken(token);

    // Reduce token stream (optimizes tokens into more significant types)
    tokens = new Reducer(tokens).reduceTokenStream();

    return tokens;
  }


  private StringBuilder bakeToken(StringBuilder token) {
    // short circuit zero-length tokens.
    if (token.length() == 0)
      return null;

    process(token.toString());

    token = new StringBuilder();
    inToken = false;
    isNumeric = true;
    return token;
  }

  private void process(String stringToken) {
    // Do something with this token. Decide if it is an identifier or not?
    TokenKind tokenKind;

    if (isNumeric) {
      tokenKind = TokenKind.INTEGER;
    } else {
      tokenKind = TokenKind.resolve(stringToken);
    }

    if (null == tokenKind) {
      // add compiler error.
      throw new RuntimeException("compile error, unrecognized symbol: " + stringToken);
    }

    final Token token = new Token(stringToken, tokenKind);
    tokens.add(token);
  }
}
