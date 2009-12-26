package purple;

import org.junit.Test;

import java.util.List;
import java.util.Arrays;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class TokenizerTest {

  @Test
  public final void simpleExpressions() {
    new Tokenizer("x + y").tokenize();
    new Tokenizer("22.0 + 33").tokenize();
    new Tokenizer("x.y + 2.0").tokenize();
  }

  @Test
  public final void decimal() {
    assert Arrays.asList(
        new Token("11.0", TokenKind.DECIMAL)
        ).equals(new Tokenizer("11.0").tokenize());
  }

  @Test
  public final void decimalWithDotCall() {
    assert Arrays.asList(
        new Token("11.0", TokenKind.DECIMAL),
        new Token(".", TokenKind.DOT),
        new Token("increment", TokenKind.IDENT)
        ).equals(new Tokenizer("11.0.increment").tokenize());
  }

  @Test
  public final void eols() {
    assert Arrays.asList(
        new Token("\n", TokenKind.EOL),
        new Token("(", TokenKind.LPAREN),
        new Token("1", TokenKind.INTEGER),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("2.4", TokenKind.DECIMAL),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.RPAREN),
        new Token("\n", TokenKind.EOL)
        ).equals(new Tokenizer("\n\n\n(\n 1 \n+ \n2.4)\n\n").tokenize());
  }

  @Test
  public final void thunk() {
    assert Arrays.asList(
        new Token("def", TokenKind.DEF),
        new Token("thunk", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token(")", TokenKind.RPAREN),
        new Token(":", TokenKind.COLON),
        new Token("{", TokenKind.LBRACE),
        new Token("1.0", TokenKind.DECIMAL),
        new Token("}", TokenKind.RBRACE)
    ).equals(new Tokenizer("def thunk: \n 1.0").tokenize());
  }

  @Test
  public final void thunkUntouched() {
    assert Arrays.asList(
        new Token("def", TokenKind.DEF),
        new Token("thunk", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token(")", TokenKind.RPAREN),
        new Token(":", TokenKind.COLON),
        new Token("{", TokenKind.LBRACE),
        new Token("1.0", TokenKind.DECIMAL),
        new Token("}", TokenKind.RBRACE)
    ).equals(new Tokenizer("def thunk: { 1.0 }").tokenize());
  }

  @Test
  public final void functionDefinition() {
    new Tokenizer("def +(x, y):" +
        " x.add_to(y) ").tokenize();
  }

  @Test
  public final void doBlockFunctionDefinition() {
    new Tokenizer("def +(x, y): {" +
        " x.add_to(y)\n" +
        " x.add_to(0) \n" +
        "}").tokenize();
  }

  @Test
  public final void infixCallRewrite() {
    List<Token> list = new Tokenizer("x + y + z").tokenize();
    System.out.println(new Stringizer().detokenize(list.toArray(new Token[list.size()])));
                    
    // x + y + z -> x.+(y.+(z))
    assert Arrays.asList(
        new Token("x", TokenKind.IDENT),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("y", TokenKind.IDENT),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("z", TokenKind.IDENT),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.RPAREN)
    ).equals(list) : list;
  }

  @Test
  public final void infixCallRewriteWithGrouping() {
    List<Token> list = new Tokenizer("(x + y) + z").tokenize();

    System.out.println(new Stringizer().detokenize(list.toArray(new Token[list.size()])));

    // (x + y) + z -> (x.+(y)).+(z)
    List<Token> expected = Arrays.asList(
        new Token("(", TokenKind.LPAREN),
        new Token("x", TokenKind.IDENT),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("y", TokenKind.IDENT),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.RPAREN),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("z", TokenKind.IDENT),
        new Token(")", TokenKind.RPAREN)
    );

    System.out.println(new Stringizer().detokenize(expected.toArray(new Token[10])));

    assert expected.equals(list) : list;
  }

  @Test
  public final void infixCallRewriteWithMoreGrouping() {
    List<Token> list = new Tokenizer("(x + (y)) + (z - d)").tokenize();

    System.out.println(new Stringizer().detokenize(list.toArray(new Token[0])));

    // (x + (y)) + (z - d) -> (x.+(y)).+(z.-(d))
    List<Token> expected = Arrays.asList(
        new Token("(", TokenKind.LPAREN),
        new Token("x", TokenKind.IDENT),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("(", TokenKind.LPAREN),
        new Token("y", TokenKind.IDENT),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.RPAREN),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("(", TokenKind.LPAREN),
        new Token("z", TokenKind.IDENT),
        new Token(".", TokenKind.DOT),
        new Token("-", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("d", TokenKind.IDENT),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.RPAREN)
    );

    assert expected.equals(list) : list;
  }
}
