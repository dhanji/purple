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
    new Tokenizer("x.y + -2.0").tokenize();
  }

  @Test
  public final void functionDefinition() {
    new Tokenizer("def +(x, y)" +
        " x.add_to(y) ").tokenize();
  }

  @Test
  public final void doBlockFunctionDefinition() {
    new Tokenizer("def +(x, y) {" +
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
        new Token("(", TokenKind.GROUPING_LPAREN),
        new Token("x", TokenKind.IDENT),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("y", TokenKind.IDENT),
        new Token(")", TokenKind.RPAREN),
        new Token(")", TokenKind.GROUPING_RPAREN),
        new Token(".", TokenKind.DOT),
        new Token("+", TokenKind.IDENT),
        new Token("(", TokenKind.LPAREN),
        new Token("z", TokenKind.IDENT),
        new Token(")", TokenKind.RPAREN)
    );

    System.out.println(new Stringizer().detokenize(expected.toArray(new Token[10])));

    assert expected.equals(list) : list;
  }


}
