package purple.syntax.model;

import java.util.List;

/**
 * A type definition.
 */
public class ClassDef implements SyntaxNode {
  private final String name;
  private final List<FieldDef> fields;

  public ClassDef(String name, List<FieldDef> fields) {
    this.name = name;
    this.fields = fields;
  }

  public String getName() {
    return name;
  }

  public List<FieldDef> getFields() {
    return fields;
  }

  @Override
  public String toString() {
    return "ClassDef{" +
        "name='" + name + '\'' +
        ", fields=" + fields +
        '}';
  }
}
