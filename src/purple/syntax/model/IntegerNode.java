package purple.syntax.model;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class IntegerNode implements SyntaxNode {
  private final int value;

  public IntegerNode(int value) {
    this.value = value;
  }

  public int getValue() {
    return value;
  }

  @Override
  public String toString() {
    return "IntegerNode{" +
        "value=" + value +
        '}';
  }
}
