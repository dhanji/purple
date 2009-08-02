package purple.interpret;

import purple.syntax.Parser;
import purple.Tokenizer;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Interpreter {

  public String interpret(String expression) {
    return new Parser(new Tokenizer(expression).tokenize())
        .parse().toString();
//        .evaluate();
  }
}
