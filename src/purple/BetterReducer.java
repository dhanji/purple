package purple;

import purple.syntax.PurpleGrammarException;

import java.util.ArrayList;
import java.util.List;

/**
 * An improvement over the crappy old reducer.
 *
 * This is an LL(k) parser that regularizes the rather liberal
 * grammar we allow in the language. The regularized token stream
 * is then parsed by a depth-recursive LR parser that generates
 * the program model.
 */
public class BetterReducer {
  private List<Token> tokens;

  public BetterReducer(List<Token> tokens) {
    this.tokens = tokens;
  }

  public List<Token> reduceTokenStream() {
    tokens = normalizeDecimals();
    tokens = normalizeThunks();
    tokens = reduceNewlines();
    tokens = normalizeOnelineFuncsAndGrouping();
    tokens = rewriteInfixCallsAsPostfix();

    return tokens;
  }

  /**
   * Rewrites function calls in infix notation as dot-based postfix notation.
   * So goes from,
   *
   * x + y + z
   *
   * to
   *
   * x.+(y.+(z))
   * 
   */
  private List<Token> rewriteInfixCallsAsPostfix() {
    int size = tokens.size();
    List<Token> out = new ArrayList<Token>(size);

    int state = FREE;
    int infixWrap = 0;
    for (int i = 0; i < size; i++) {
      Token token = tokens.get(i);

      // Account for function signatures
      if (token.isDef() && isNext(i, TokenKind.IDENT)) {
        state = IN_FUNC_SIG;
      } else if (token.is(TokenKind.COLON) && state == IN_FUNC_SIG) {
        // out of func sig
        state = FREE;
      }

      // Class definitions also should be skipped.
      if (token.is(TokenKind.CLASS) && isNext(i, TokenKind.TYPE_IDENT, TokenKind.COLON)) {
        state = IN_CLASS_DEF;
      }

      // end class def.
      if (state == IN_CLASS_DEF && token.is(TokenKind.RBRACE)) {
        state = FREE;
      }

      // We have to skip function signatures.
      if (state != IN_FUNC_SIG && state != IN_CLASS_DEF) {

        // (Expr) IDENT (Expr)
        if (isAtom(token) && isNext(i, TokenKind.IDENT)) {

          out.add(token);
          out.add(Token.dot());
          out.add(lookAhead(i, 1));
          out.add(Token.lparen());

          // skip the two idents
          i += 1;

          infixWrap++;
          continue;
        }

        if (token.isExpressionDelimiter() && infixWrap > 0) {
          // close wrap
          out.add(token);
          out.add(Token.rparen());
          infixWrap--;
          continue;
        }
      }
      out.add(token);
    }

    // End of file writes.
    while (infixWrap > 0) {
      // close wrap
      out.add(Token.rparen());
      infixWrap--;
    }
    return out;
  }

  private static boolean isAtom(Token back) {
    return back.is(TokenKind.IDENT)
        || back.is(TokenKind.RPAREN)
        || back.is(TokenKind.DECIMAL)
        || back.is(TokenKind.INTEGER)
        || back.is(TokenKind.REGEX)
        || back.is(TokenKind.STRING)
        || back.is(TokenKind.TYPE_IDENT);
  }

  private List<Token> normalizeDecimals() {
    int size = tokens.size();
    List<Token> out = new ArrayList<Token>(size);

    // normalize decimal literals.
    for (int i = 0; i < size; i++) {
      Token token = tokens.get(i);

      // INT DOT INT
      if (token.isInteger() && isNext(i, TokenKind.DOT, TokenKind.INTEGER)) {
        out.add(new Token(String.format("%s.%s", token.getName(),
            lookAhead(i, 2).getName()), TokenKind.DECIMAL));

        // skip the dot and int
        i += 2;
        continue;
      }

      out.add(token);
    }
    return out;
  }

  private static final int FREE = 0;
  private static final int IN_FUNC_SIG = 1;
  private static final int IN_FUNC_BODY = 2;
  private static final int IN_CLASS_DEF = 3;
  private List<Token> normalizeOnelineFuncsAndGrouping() {
    int size = tokens.size();
    List<Token> out = new ArrayList<Token>(size);

    int state = FREE;
    for (int i = 0; i < size; i++) {
      Token token = tokens.get(i);


      /**
       * This turns,
       * def thunk():
       *   1
       *
       * into
       *
       * def thunk(): {
       *   1
       * }
       *
       * ..to help out the parser.
       */
      // DEF IDENT COLON
      if (token.isDef() && isNext(i, TokenKind.IDENT)) {
        state = IN_FUNC_SIG;
      }

      if (token.is(TokenKind.COLON) && state == IN_FUNC_SIG) {
        state = IN_FUNC_BODY;

        // We dont need to do anything if there is already a do block here.
        if (isNext(i, TokenKind.LBRACE) || isNext(i, TokenKind.EOL, TokenKind.LBRACE)) {
          state = FREE;

          // skip this eol, it serves no purpose.
          if (isNext(i, TokenKind.EOL)) {
            i++;
          }

        } else {
          out.add(token);
          out.add(new Token("{", TokenKind.LBRACE));

          // skip this eol, it serves no purpose.
          if (isNext(i, TokenKind.EOL)) {
            i++;
          }
          continue;
        }
      }

      // End do block after a line. And get rid of stupid eols.
      if (token.isEol() && state == IN_FUNC_BODY) {
        out.add(new Token("}", TokenKind.RBRACE));
        state = FREE;
        continue;
      }

      out.add(token);
    }

    // unterminated do block.
    if (state == IN_FUNC_BODY) {
        out.add(new Token("}", TokenKind.RBRACE));
    }

    return out;
  }

  private List<Token> normalizeThunks() {
    int size = tokens.size();
    List<Token> out = new ArrayList<Token>(size);

    // normalize decimal literals.
    for (int i = 0; i < size; i++) {
      Token token = tokens.get(i);

      /**
       * This turns,
       * def thunk:
       *
       * into
       *
       * def thunk():
       *
       * ..to help out the parser.
       */
      // DEF IDENT COLON
      if (token.isDef() && isNext(i, TokenKind.IDENT, TokenKind.COLON)) {

        // add DEF IDENT LPAREN RPAREN COLON
        out.add(token);
        out.add(lookAhead(i, 1));
        out.add(new Token("(", TokenKind.LPAREN));
        out.add(new Token(")", TokenKind.RPAREN));
        out.add(lookAhead(i, 2));

        // skip the ident and colon (coz we just added them)
        i += 2;
        continue;
      }

      out.add(token);
    }
    return out;
  }

  /**
   * This turns a sequence of newlines into just one.
   */
  private List<Token> reduceNewlines() {
    int size = tokens.size();
    List<Token> out = new ArrayList<Token>(size);

    int paren = 0;
    for (int i = 0; i < size; i++) {
      Token token = tokens.get(i);

      // EOL+
      if (token.isEol() && isNext(i, TokenKind.EOL)) {
        continue;
      }

      if (token.is(TokenKind.LPAREN)) {
        paren++;
      } else if (token.is(TokenKind.RPAREN)) {
        paren--;
      }

      out.add(token);
    }

    if (paren != 0) {
      throw new PurpleGrammarException("Unbalanced ()");
    }
    return out;
  }

  public boolean isNext(int start, TokenKind... kinds) {
    for (TokenKind kind : kinds) {
      Token next = lookAhead(start, 1);
      if (null == next) {
        return false;
      }
      if (!kind.equals(next.getKind())) {
        return false;
      }

      start++;
    }
    return true;
  }

  public Token lookAhead(int start, int distance) {
    int i = start + distance;
    if (i < tokens.size()) {
      return tokens.get(i);
    }
    return null;
  }
}
