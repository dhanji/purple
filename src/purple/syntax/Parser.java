package purple.syntax;

import purple.Token;
import purple.TokenKind;
import purple.syntax.model.*;

import java.util.List;
import java.util.ArrayList;

/**
 * The reducer converts a purple token stream into an eval tree
 * (i.e. a data model representation that is evaluable as the program
 * output).
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Parser {

  private final List<Token> tokens;

  public Parser(List<Token> tokens) {
    this.tokens = tokens;
  }

  // stateful parser, maintains offset into token stream
  // to skip ahead on returning from a parsing rule.
  private int skip = 0;

  public SyntaxNode parse() {
    System.out.println(tokens);
    return parseRange(0, tokens.size());
  }

  private SyntaxNode parseRange(int index, int length) {
    SyntaxNode node = null;

    for (;index < length; index++) {
      Token token = tokens.get(index);

      if (TokenKind.DEF == token.getKind()) {
        functionDef(index);
      }


      // process first token as an expression (literal, var, etc.)
      node = processToken(token);
      final Token next = lookAhead(index, 1);

      // if the next token is a dot, then this whole thing is a
      // postfix function call.
      if (null != next && TokenKind.DOT == next.getKind()) {

        // 2 ahead should be an ident.
        skip = 2;
        final Token twoAhead = lookAhead(index, 2);
        check(TokenKind.IDENT == twoAhead.getKind(), "Expected function name after .");

        // replace root expression node with a wrapped function call node.
        List<SyntaxNode> args = new ArrayList<SyntaxNode>();
        args.add(node);
        node = new FunctionCall(twoAhead.getName(), parseArgList(index + skip, args));

        // skip over function call tokens
        index += skip;
      }
    }

    return node;
  }

  /**
   * Parsing rule for function definitions.
   */
  private void functionDef(int index) {
    // This is a function definition.
    Token funcName = lookAhead(index, 1);
    Token lparen = lookAhead(index, 2);
    boolean isThunk = lparen.getKind() == TokenKind.COLON;

    System.out.println("function name = " + funcName);
    check(funcName.getKind() == TokenKind.IDENT, "def must be followed by a valid identifier");
    check(lparen.getKind() == TokenKind.LPAREN || isThunk,
        "function def signature must contain arg list (..) or :");

    // skip function name and colon/lparen
    skip += 2;
    if (isThunk) {
      // look for starting brace
      Token rbrace = lookAhead(index, 3);

      check(TokenKind.LBRACE != rbrace.getKind(),
          "Function body parsing error, no post-processed do block available! (indicates a parsing bug)");
      skip++;

      // Find the balancing right brace:
      int endAt = balancedSeek(TokenKind.LBRACE, TokenKind.RBRACE, index + 4);
      check(endAt != -1, "Missing } in function definition, tokenization bug?");

      // parse normally, recursively.
      SyntaxNode functionDoBlock = parseRange(index + 4, endAt);
      System.out.println("Stuff: " + functionDoBlock);
    }
  }

  /**
   * Parses a parenthetical list of tokens into a stream of syntax nodes.
   */
  private SyntaxNode[] parseArgList(int index, List<SyntaxNode> args) {
    // if 3 ahead is an lparen, then we have an arg list
    final Token firstToken = lookAhead(index, 1);
    boolean isBalanced = true;

    if (null != firstToken && TokenKind.LPAREN == firstToken.getKind()) {
      skip++;

      // Split token stream into an argument list
      for (int j = index + skip; j < tokens.size(); j++, skip++) {
        final Token current = tokens.get(j);

        if (TokenKind.COMMA == current.getKind()) {
          // bake away into an argument expression
          args.add(parseRange(index + 2, j));

          // move start cursor past last comma.
          // and also skip comma and last token.
          index = j - 1;
          skip += (j - index + 2);

        } else if (TokenKind.RPAREN == current.getKind()) {

          // bake away into the last argument expression, unless
          // this is an empty argument list.
          if (TokenKind.LPAREN != lookAhead(j, -1).getKind()) {

            args.add(parseRange(index + 2, j));
            skip += (j - index + 2);
          }

          // Closed properly.
          return args.toArray(new SyntaxNode[args.size()]);
        }
      }

      // is balanced?
      check(isBalanced, "Missing ) in parenthetical function call");
    }

    skip++;
    return args.toArray(new SyntaxNode[args.size()]);
  }

  private static void check(boolean condition, String message) {
    // TODO: make this more sexy.
    if (!condition)
      throw new PurpleGrammarException(message);
  }

  private SyntaxNode processToken(Token token) {
    // Interpret literals as they are
    if (TokenKind.INTEGER == token.getKind()) {
      return new IntegerLiteral(Integer.parseInt(token.getName()));
    } else if (TokenKind.DECIMAL == token.getKind()) {
      return new Decimal(Double.parseDouble(token.getName()));
    }

    // otherwise validate and treat as identifier...
    return new Variable(token.getName());
  }


  private Token lookAhead(int from, int ahead) {
    if (tokens.size() > from + ahead) {
      return tokens.get(from + ahead);
    }

    return null;
  }

  private int balancedSeek(TokenKind scopeStart, TokenKind seek, int startAt) {
    int scopes = 0;
    for (int i = startAt; i < tokens.size(); i++) {
      Token token = tokens.get(i);
      if (scopeStart == token.getKind())
        scopes++;

      if (seek == token.getKind()) {

        // we skip closing tokens for ones we've already seen.
        if (scopes == 0) {
          return i;
        }

        scopes--;
      }
    }

    // Failed to find anything
    return -1;
  }
}