package purple.syntax.model;

import java.util.Arrays;

/**
 * A special type of function call that expresses a sequential invocation
 * of multiple functions, evaluating to the return value of the last one.
 */
public class DoBlock extends FunctionCall {
  private final SyntaxNode[] sequence;

  public DoBlock(SyntaxNode[] sequence) {
    super("DOBLOCK", new SyntaxNode[0]);
    this.sequence = sequence;
  }

  public SyntaxNode[] getSequence() {
    return sequence;
  }

  @Override
  public final boolean isDoBlock() {
    return true;
  }

  @Override
  public String toString() {
    return "DoBlock{" +
        "sequence=" + Arrays.asList(sequence) +
        '}';
  }
}
