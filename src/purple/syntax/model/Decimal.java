package purple.syntax.model;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Decimal implements SyntaxNode {
  private final double value;

  public Decimal(double value) {
    this.value = value;
  }

  public double getValue() {
    return value;
  }
}
