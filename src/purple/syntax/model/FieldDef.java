package purple.syntax.model;

/**
 * A field definition as member of a type.
 */
public class FieldDef implements SyntaxNode {
  private final String name;
  private final ClassDef type;

  public FieldDef(String name, ClassDef type) {
    this.name = name;
    this.type = type;
  }

  public String getName() {
    return name;
  }

  public ClassDef getType() {
    return type;
  }

  @Override
  public String toString() {
    return "FieldDef{" +
        "name='" + name + '\'' +
        ", type=" + type.getName() +
        '}';
  }
}