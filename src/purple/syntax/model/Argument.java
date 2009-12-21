package purple.syntax.model;

/**
 * Function argument.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class Argument implements SyntaxNode {
  private final String name;
  private final String type; // this will probably change to its own kind

  public Argument(String name, String type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public String getType() {
    return type;
  }

  @Override
  public String toString() {
    return "Argument{" +
        "type=" + type +
        ", name=" + name +
        '}';
  }
}