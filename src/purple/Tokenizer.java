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
    reduceTokenStream();

    return tokens;
  }

  private void reduceTokenStream() {
    List<Token> out = new ArrayList<Token>();

    int infixWraps = 0;  //number of times we've rewritten an infix as postfix parenthetical
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      Token next = lookAhead(i, 1);

      // reduce number (decimal) tokens
      if (TokenKind.INTEGER == token.getKind()) {
        Token nextNext = lookAhead(i, 2);

        // if next is . and nextNext is an integer, then reduce to a decimal
        if (null != nextNext && null != next) {
          if (TokenKind.DOT == next.getKind() && TokenKind.INTEGER == nextNext.getKind()) {

            // Maybe improve this some day.
            out.add(new Token(String.format("%s.%s", token.getName(), nextNext.getName()), TokenKind.DECIMAL));
            i += 2;
            continue;
          }
        }
      }

      if (TokenKind.DEF != token.getKind()) {

        // Reduce infix calls by rewriting them as postfix dot-notation calls
        if (null != next
            && TokenKind.DOT != token.getKind()
            && TokenKind.IDENT == next.getKind()) {

          out.add(token);
          out.add(new Token(".", TokenKind.DOT));
          out.add(next);
          out.add(new Token("(", TokenKind.LPAREN));
          infixWraps++;

          // skip ahead 1
          i++;
          continue;
        }
      } else {
        // wrap free def functions as a { } do block.
        
      }

      
      // otherwise do a dumb copy
      out.add(token);
    }

    // terminate all infix psuedo-wraps
    for (int x = 0; x < infixWraps; x++)
      out.add(new Token(")", TokenKind.RPAREN));

    //replace token stream with reduced stream
    tokens = out;
  }

  private Token lookAhead(int i, int ahead) {
    if (tokens.size() > i + ahead) {
      return tokens.get(i + ahead);
    }

    return null;
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
