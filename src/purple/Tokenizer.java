package purple;

import purple.syntax.PurpleGrammarException;

import java.util.*;

/**
 * The language architecture works like this:
 *
 * Tokenizer -> Reducer -> Parser -> model
 *
 * The Tokenizer produces tokens from the raw source file. This
 * token list is then optimized, condensed and regularized by the
 * Reducer (for example, grouping parentheses are added) including
 * syntactic sugar elements.
 *
 * Finally, the parser goes through the optimized token stream
 * and builds a semantic program model from it, consisting of
 * control flow structures, function declarations and so on.
 *
 * The model is then further processed by a compiler to produce
 * an executable binary. Or it may be processed by an interpreter
 * or other meta analysis tool. 
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Tokenizer {
  private final String expression;
  private final char[] expr;

  public Tokenizer(String expression) {
    this.expression = expression;
    this.expr = expression.toCharArray();
  }

  /**
   * These are tokens that are of a single character's width.
   */
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
    int inParen = 0;

    StringBuilder token = new StringBuilder();
    for (int i = 0; i < expr.length; i++) {
      char c = expr[i];

      // This helps us skip newlines in () groups.
      // TODO(dhanji): watch out for string and regex literals
      if ('(' == c) {
        inParen++;
      } else if (')' == c) {
        inParen--;
      }

      // Add EOLs (and see if the world asplodes)
      if ('\n' == c && inParen == 0) {
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
    // Do something with this token. Decide if it is an identifier or not.
    TokenKind tokenKind;

    if (isNumeric) {
      tokenKind = TokenKind.INTEGER;
    } else {
      tokenKind = TokenKind.resolve(stringToken);
    }

    if (null == tokenKind) {
      // add compiler error.
      throw new PurpleGrammarException("compile error, unrecognized symbol: " + stringToken);
    }

    tokens.add(new Token(stringToken, tokenKind));
  }
}
