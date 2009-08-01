package purple.syntax.model;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Variable implements SyntaxNode {
  private final String name;

  public Variable(String name) {
    this.name = name;
  }

  public String getName() {
    return name;
  }

  @Override
  public String toString() {
    return "Variable{" +
        "name=" + name +
        '}';
  }
}