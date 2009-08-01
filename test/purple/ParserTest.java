package purple;

import org.junit.Test;
import purple.syntax.model.*;
import purple.syntax.Parser;

/**
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class ParserTest {

  @Test
  public final void integer() {
    SyntaxNode node =
        new Parser(new Tokenizer("11")
            .tokenize()).parse();

    assert node instanceof IntegerLiteral;
    assert ((IntegerLiteral) node).getValue() == 11;
  }

  @Test
  public final void postfixFunctionCall() {
    SyntaxNode node =
        new Parser(new Tokenizer("11.increment")
            .tokenize()).parse(
        );

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "increment".equals(call.getName());
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;
  }

  @Test
  public final void decimalPostfixParentheticalFunctionCall() {
    SyntaxNode node =
        new Parser(new Tokenizer("11.0.increment()")
            .tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "increment".equals(call.getName());
    assert call.getArgs()[0] instanceof Decimal;
    assert ((Decimal) call.getArgs()[0]).getValue() == 11.0;
  }

  @Test
  public final void postfixFunctionCallWithOneParentheticalArg() {
    SyntaxNode node =
        new Parser(new Tokenizer("11.+(1)").tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert call.getArgs()[1] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;
    assert ((IntegerLiteral) call.getArgs()[1]).getValue() == 1;
  }

  @Test
  public final void postfixFunctionCallWithParentheticalArgs() {
    SyntaxNode node =
        new Parser(new Tokenizer("11.+(1, 3.5)")
            .tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 3;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert call.getArgs()[1] instanceof IntegerLiteral;
    assert call.getArgs()[2] instanceof Decimal;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;
    assert ((IntegerLiteral) call.getArgs()[1]).getValue() == 1;
    assert ((Decimal) call.getArgs()[2]).getValue() == 3.5;
  }

  @Test
  public final void postfixFunctionCallWithParentheticalArgsAndNesting() {
    SyntaxNode node =
        new Parser(new Tokenizer("11.+(1, 5.add(1))")
            .tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 3;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert call.getArgs()[1] instanceof IntegerLiteral;
    assert call.getArgs()[2] instanceof FunctionCall;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;
    assert ((FunctionCall) call.getArgs()[2]).getArgs().length == 2;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[2];
    assert "add".equals(innerCall.getName());
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 5;
    assert ((IntegerLiteral)innerCall.getArgs()[1]).getValue() == 1;
  }

  @Test
  public final void infixFunctionCall() {
    SyntaxNode node =
        new Parser(new Tokenizer("11 + 4")
            .tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert call.getArgs()[1] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;
    assert ((IntegerLiteral) call.getArgs()[1]).getValue() == 4;
  }

  @Test
  public final void multiLineInfixFunctionCall() {
    SyntaxNode node =
        new Parser(new Tokenizer("11 +" +
            "\n\n 4")
            .tokenize()).parse(
        );

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert call.getArgs()[1] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;
    assert ((IntegerLiteral) call.getArgs()[1]).getValue() == 4;
  }

  @Test
  public final void compoundInfixFunctionCall() {
    SyntaxNode node =
        new Parser(new Tokenizer("11 + 4 + 3")
            .tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert call.getArgs()[1] instanceof FunctionCall : call;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[1];
    assert "+".equals(innerCall.getName());
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert innerCall.getArgs()[1] instanceof IntegerLiteral;
    assert ((IntegerLiteral) innerCall.getArgs()[0]).getValue() == 4;
    assert ((IntegerLiteral) innerCall.getArgs()[1]).getValue() == 3;

  }

  @Test
  public final void variablizedInfixFunctionCall() {
    SyntaxNode node =
        new Parser(new Tokenizer("a + b + c")
            .tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof Variable;
    assert call.getArgs()[1] instanceof FunctionCall : call;
    assert "a".equals(((Variable) call.getArgs()[0]).getName());

    FunctionCall innerCall = (FunctionCall) call.getArgs()[1];
    assert "+".equals(innerCall.getName());
    assert innerCall.getArgs()[0] instanceof Variable;
    assert innerCall.getArgs()[1] instanceof Variable;
    assert "b".equals(((Variable) innerCall.getArgs()[0]).getName());
    assert "c".equals(((Variable) innerCall.getArgs()[1]).getName());

  }
}
