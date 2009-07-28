package purple.syntax;

import purple.Token;
import purple.TokenKind;
import purple.syntax.model.SyntaxNode;
import purple.syntax.model.IntegerNode;

import java.util.List;

/**
 * The reducer converts a purple token stream into an eval tree
 * (i.e. a data model representation that is evaluable as the program
 * output).
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Parser {

  public void parse(List<Token> tokens) {
    
    
    for (int i = 0; i < tokens.size(); i++) {
      Token token = tokens.get(i);

      // process first token as is.
      SyntaxNode node = processToken(token);

      // if the next token is a dot, then this whole thing is a
      // postfix function call.
      final Token next = lookAhead(tokens, i, 1);
      if (null != next && TokenKind.DOT == next.getKind()) {
        
      }
    }
  }

  private SyntaxNode processToken(Token token) {
    // Interpret literals as they are
    if (TokenKind.INTEGER == token.getKind()) {
      return new IntegerNode(Integer.parseInt(token.getName()));
    }

    // Parsing error
    throw new RuntimeException("Symbol not understood: " + token);
  }


  private Token lookAhead(List<Token> tokens, int i, int ahead) {
    if (tokens.size() > i + ahead) {
      return tokens.get(i + ahead);
    }

    return null;
  }
}