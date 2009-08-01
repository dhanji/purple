package purple.syntax.model;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class DecimalNode implements SyntaxNode {
  private final double value;

  public DecimalNode(double value) {
    this.value = value;
  }

  public double getValue() {
    return value;
  }
}
