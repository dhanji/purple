package purple.syntax.model;

import java.util.Arrays;

/**
 * Represents a single file or partial modular unit of code. Still of
 * smaller fragment than a module. In other words the atomic compilation unit.
 */
public class Script implements SyntaxNode {
  private final String name;
  private final SyntaxNode[] sequence;

  public Script(String name, SyntaxNode[] sequence) {
    this.name = name;
    this.sequence = sequence;
  }

  public String getName() {
    return name;
  }

  public SyntaxNode[] getSequence() {
    return sequence;
  }

  @Override
  public String toString() {
    return "Script{" +
        "name='" + name + '\'' +
        ", sequence=" + (sequence == null ? null : Arrays.asList(sequence)) +
        '}';
  }
}
