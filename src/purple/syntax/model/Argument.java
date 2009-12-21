package purple.syntax.model;

/**
 * Function argument.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Argument implements SyntaxNode {
  private final String name;

  public Argument(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Argument{" +
        "name=" + name +
        '}';
  }
}