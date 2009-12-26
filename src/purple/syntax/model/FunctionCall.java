package purple.syntax.model;

import java.util.Arrays;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class FunctionCall implements SyntaxNode {
  private final String name;
  private final SyntaxNode[] args;

  public FunctionCall(String name, SyntaxNode[] args) {
    this.name = name;
    this.args = args;
  }

  public String getName() {
    return name;
  }

  public SyntaxNode[] getArgs() {
    return args;
  }

  public boolean isDoBlock() {
    return false;
  }

  @Override
  public String toString() {
    return "FunctionCall{" +
        "name='" + name + '\'' +
        ", args=" + (args == null ? null : Arrays.asList(args)) +
        '}';
  }
}
