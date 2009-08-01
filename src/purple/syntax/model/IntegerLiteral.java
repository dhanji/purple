package purple.syntax.model;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class IntegerLiteral implements SyntaxNode {
  private final int value;

  public IntegerLiteral(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "IntegerLiteral{" +
        "value=" + value +
        '}';
  }
}
