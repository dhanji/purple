package purple;

import java.util.List;
import java.util.ArrayList;

/**
 * @author dhanji@google.com (Dhanji R. Prasanna)
 */
class Reducer {
  private List<Token> tokens;

  public Reducer(List<Token> tokens) {
    this.tokens = tokens;
  }

  public List<Token> reduceTokenStream() {

    // Initial pass tokenization to normalize between parenthetical function calls
    // and expression grouping parentheticals.
    tokens = normalizeGroupingParens();


    // Progressive pass tokenization to rewrite infix function calls.
    List<Token> out = new ArrayList<Token>();

    int infixWraps = 0;  //number of times we've rewritten an infix as postfix parenthetical
    int parenthetical = 0;

    // State variable tracks when we are processing a function.
    boolean inFunctionDef = false;
    boolean inFunctionSignature = false;
    boolean shouldWriteRBrace = false; // needed to close the {} in a function def.

    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      Token next = lookAhead(i, 1);

      // We don't need to write automagic infix wrapping rparen )
      // if encountering a group closure (coz it will do it for us)
      if (TokenKind.GROUPING_RPAREN == token.getKind()) {
        infixWraps--;
      }

      // Guards against simple parenthetical blocks () <-- non scoping
      if (TokenKind.LPAREN == token.getKind()) {
        parenthetical++;
      } else if (TokenKind.RPAREN == token.getKind()) {
        parenthetical--;
      } else if (TokenKind.EOL == token.getKind() && parenthetical > 0) {
        // drain newlines inside an explicit parenthetical expression
        continue;
      } else if (null != next
          && TokenKind.EOL == token.getKind()
          && TokenKind.EOL == next.getKind()) {
        // collapse multiple consecutive newlines into one.
        continue;
      }

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


      // wraps a function body in { } braces (do block), but only if it's not already.
      if (inFunctionDef 
          && TokenKind.COLON == token.getKind()) {

        out.add(token);
        out.add(new Token("{", TokenKind.LBRACE));

        // out of function signature.
        inFunctionSignature = false;
        
        // skip ahead 1
        i++;
        continue;
      }

      if (inFunctionDef && TokenKind.EOL == token.getKind()) {
        if (shouldWriteRBrace) {
          // Terminate infix wraps
          terminateInfixWraps(out, infixWraps);
          infixWraps = 0;
          
          out.add(new Token("}", TokenKind.RBRACE));

          // close function
          shouldWriteRBrace = false;
          inFunctionDef = false;
        } else {
          shouldWriteRBrace = true;
        }
      }

      if (TokenKind.DEF != token.getKind()) {

        // Reduce infix calls by rewriting them as postfix dot-notation calls
        if (null != next
            && !inFunctionSignature
            && TokenKind.DOT != token.getKind()
            && TokenKind.LPAREN != token.getKind()
            && TokenKind.GROUPING_LPAREN != token.getKind()
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
        inFunctionDef = true;
        inFunctionSignature = true;
      }


      // otherwise do a dumb copy
      out.add(token);
    }


    // terminate all infix psuedo-wraps
    terminateInfixWraps(out, infixWraps);

    // Terminate any function defs.
    if (shouldWriteRBrace) {
      out.add(new Token("}", TokenKind.RBRACE));
    }

    //replace token stream with reduced stream
    tokens = out;

    return tokens;
  }

  private void terminateInfixWraps(List<Token> out, int infixWraps) {
    for (int x = 0; x < infixWraps; x++) {
      out.add(new Token(")", TokenKind.RPAREN));
    }
  }

  private List<Token> normalizeGroupingParens() {
    int tokenCount = tokens.size();
    List<Token> out = new ArrayList<Token>(tokenCount);
    boolean inGroupingBlock = false;
    for (int i = 0; i < tokenCount; i++) {
      Token token = tokens.get(i);
      Token backTwo = null;

      // Make sure we dont slide below the 0 index.
      if (i > 1) {
        backTwo = lookAhead(i, -2);
      }

      // Reduce redundant grouping. i.e groups like: (IDENT) -> IDENT
      if (i > 2 && i < tokenCount - 2) {
        Token next = lookAhead(i, 1);
        if (TokenKind.LPAREN == token.getKind()
            && isStandalone(next)
            && TokenKind.RPAREN == lookAhead(i, 2).getKind()
            && TokenKind.DOT != lookAhead(i, -2).getKind()) {

          // skip the ( and ), adding just the ident in the middle.
          out.add(next);
          i += 2;
          continue;
        }
      }


      // Ensure that two back is not a DOT, in other words, that it's not a postfix
      // function call arg list. Improve this into an if block.
      if (TokenKind.LPAREN == token.getKind()
          && ( (null != backTwo
                && (TokenKind.DOT != backTwo.getKind()
                && TokenKind.DEF != backTwo.getKind()))
              || backTwo == null)

          ) {

        // add it as an expression grouper.
        out.add(new Token("(", TokenKind.GROUPING_LPAREN));
        inGroupingBlock = true;

      } else if (TokenKind.RPAREN == token.getKind() && inGroupingBlock) {

        // add the right parenthesis first.
        out.add(token);

        // terminate this expression grouper
        out.add(new Token(")", TokenKind.GROUPING_RPAREN));
        inGroupingBlock = false;

      } else {
        out.add(token);
      }
    }
    return out;
  }

  private static boolean isStandalone(Token next) {
    return TokenKind.IDENT == next.getKind()
        || TokenKind.INTEGER == next.getKind();
  }

  // For christ's sake clean this up!
  private Token lookAhead(int i, int ahead) {
    if (tokens.size() > i + ahead) {
      return tokens.get(i + ahead);
    }

    return null;
  }
}
