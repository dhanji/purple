package purple.syntax.model;

import java.util.Arrays;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class FunctionDef implements SyntaxNode {
  private final String name;
  private final String[] args;
  private final SyntaxNode body;

  public FunctionDef(String name, String[] args, SyntaxNode body) {
    this.name = name;
    this.args = args;
    this.body = body;
  }

  public String getName() {
    return name;
  }

  public String[] getArgs() {
    return args;
  }

  public SyntaxNode getBody() {
    return body;
  }

  @Override
  public String toString() {
    return "FunctionDef{" +
        "name='" + name + '\'' +
        ", args=" + (args == null ? null : Arrays.asList(args)) +
        ", body=" + body +
        '}';
  }
}