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
    return parseRange(0, tokens.size());
  }

  private SyntaxNode parseRange(int start, int length) {
    SyntaxNode node = null;

    for (int index = start; index < length; index++) {
      Token token = tokens.get(index);

      // Parse class definition subtrees separately.
      if (token.is(TokenKind.CLASS)) {
        return classDef(index);
      }


      // Parse function definition subtrees separately.
      if (token.is(TokenKind.DEF)) {
        return functionDef(index);
      }

      // If there is already a variable here, then this is a free function call.
      // Free function call must always be the top most node in the parse branch.
      if (node instanceof Variable && start == index) {
        // LL(k) style parse: rest of stream.
        SyntaxNode freeArg = parseRange(index, length);

        return new FunctionCall(((Variable) node).getName(), new SyntaxNode[] { freeArg });
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
   * Parsing rule for type definitions.
   */
  private SyntaxNode classDef(int index) {
    Token typeName = lookAhead(index, 1);
    check(typeName.is(TokenKind.TYPE_IDENT), "Expected type name: " + typeName.getName());
    check(lookAhead(index, 2).is(TokenKind.COLON), "Expected ':' after type signature");
    check(lookAhead(index, 3).is(TokenKind.LBRACE), "Expected '{' after type signature");

    skip(3);
    index += skip;

    Token token = lookAhead(index, 1);
    List<FieldDef> fields = new ArrayList<FieldDef>();
    do {
      if (token.is(TokenKind.TYPE_IDENT)) {
        Token fieldName = lookAhead(index, 2);
        check(fieldName != null, "Missing field name");
        check(fieldName.is(TokenKind.IDENT), "Expected field name identifier after in class def");
        
        // TODO(dhanji): Replace with lookup from type pool.
        fields.add(new FieldDef(fieldName.getName(), new ClassDef(token.getName(), null)));
      }

      skip(1);
      index++;
      token = lookAhead(index, 1);

      // Reached end of the token stream?
      if (null == token) {
        break;
      }

    } while (!token.is(TokenKind.RBRACE));

    return new ClassDef(typeName.getName(), fields);
  }

  /**
   * Parsing rule for function definitions.
   */
  private FunctionDef functionDef(int index) {
    // This is a function definition.
    Token funcName = lookAhead(index, 1);
    Token lparen = lookAhead(index, 2);
    boolean isThunk = false;

    check(funcName.getKind() == TokenKind.IDENT, "def must be followed by a valid identifier");
    check(lparen.getKind() == TokenKind.LPAREN,
        "function def signature must contain an arg list (..)");

    // skip function name and lparen
    skip(2);

    if (lookAhead(index, 3).getKind() == TokenKind.RPAREN) {
      isThunk = true;

      // skip rparen/colon
      skip(2);
    }

    index += skip;


    if (isThunk) {
      // look for starting brace
      Token leftBrace = lookAhead(index, 1);

      check(TokenKind.LBRACE == leftBrace.getKind(),
          "Function body parsing error, no post-processed do block available! (parsing bug?)");
      skip(1);

      SyntaxNode functionDoBlock = doBlock(index);

      return new FunctionDef(funcName.getName(), new Argument[0], functionDoBlock);
    }

    // Non thunks, i.e. has an argument list.
    ArrayList<Argument> args = new ArrayList<Argument>();
    int argIndex = 1;
    Token arg;
    do {
      arg = lookAhead(index, argIndex);

      if (TokenKind.IDENT == arg.getKind()) {
        args.add(new Argument(arg.getName(), "unknown"));
      } else if (TokenKind.TYPE_IDENT == arg.getKind()) {
        argIndex++;

        // Add type and argument name.
        args.add(new Argument(lookAhead(index, argIndex).getName(), arg.getName()));
      }
      
      argIndex++;
    } while (TokenKind.RPAREN != arg.getKind());

    // Resize the array so we dont waste memory.
    args.trimToSize();

    // Skip all the arguments and the rparen.
    index += skip(argIndex - 2);

    // OK now chew up any legal tokens until the start of the func body
    check(TokenKind.COLON == lookAhead(index, 0).getKind(),
        "expected ':' after function signature");

//    System.out.println("dobl found -" + doBlock(index));

    return new FunctionDef(funcName.getName(), args.toArray(new Argument[args.size()]), doBlock(index));
  }

  private SyntaxNode doBlock(int index) {
    // Find the balancing right brace:
    int endAt = balancedSeek(TokenKind.LBRACE, TokenKind.RBRACE, index + 2);
    check(endAt != -1, "Missing } in function definition, tokenization bug?");

    // split into chunks delimited by EOL
    List<Pair<Integer, Integer>> ranges = new ArrayList<Pair<Integer, Integer>>();
    int start = index + 2;
    for (int i = start; i < endAt; i++) {
      Token token = tokens.get(i);

      if (token.isEol() && start < i) {
        ranges.add(Pair.of(start, i));
        start = i + 1;
      }
    }

    // Add last chunk
    if (start < endAt) {
      ranges.add(Pair.of(start, endAt));
    }

    // Is this a multi-line doblock?
    if (ranges.size() > 1) {
      List<SyntaxNode> sequence = new ArrayList<SyntaxNode>();
      for (Pair<Integer, Integer> range : ranges) {
        sequence.add(parseRange(range.first, range.second));
      }

      return new DoBlock(sequence.toArray(new SyntaxNode[sequence.size()]));
    }

    // otherwise parse normally, recursively.
    return parseRange(index + 2, endAt);
  }

  /**
   * Parses a parenthetical list of tokens into a stream of syntax nodes.
   */
  private SyntaxNode[] parseArgList(int index, List<SyntaxNode> args) {
    // if 1 ahead is an lparen, then we have an arg list
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
          skip(j - index + 2);

        } else if (TokenKind.RPAREN == current.getKind()) {

          // bake away into the last argument expression, unless
          // this is an empty argument list.
          if (TokenKind.LPAREN != lookAhead(j, -1).getKind()) {

            args.add(parseRange(index + 2, j));
            skip(j - index + 2);
          }

          // Closed properly.
          return args.toArray(new SyntaxNode[args.size()]);
        }
      }

      // is balanced?
      check(isBalanced, "Missing ) in parenthetical function call");
    }

    skip(1);
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
    } else if (token.is(TokenKind.LPAREN) || token.is(TokenKind.RPAREN)) {
      return null; // skip parens
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

  private int skip(int skip) {
    return this.skip += skip;
  }
}