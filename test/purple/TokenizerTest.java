package purple;

import org.junit.Test;
import purple.syntax.Parser;
import purple.syntax.model.SyntaxNode;

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


}
