package purple;

import org.junit.Test;
import purple.syntax.model.*;
import purple.syntax.Parser;

/**
 * Tests that ensure basic model generation from source code fragments.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class BasicParsingTest {

  @Test
  public final void integer() {
    SyntaxNode node =
        new Parser(new Tokenizer("11")
            .tokenize()).parse();

    assert node instanceof IntegerLiteral;
    assert ((IntegerLiteral) node).getValue() == 11;
  }
  
  @Test
  public final void freeFunctionCall() {
    String putsOnePlusTwo = "puts(1 + 2)";
    SyntaxNode node =
        new Parser(new Tokenizer(putsOnePlusTwo)
            .tokenize()).parse();
    
    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;
    assert "puts".equals(call.getName());
    assert call.getArgs().length == 1;
    assert call.getArgs()[0] instanceof FunctionCall;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[0];
    assert "+".equals(innerCall.getName());
    assert innerCall.getArgs().length == 2;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert innerCall.getArgs()[1] instanceof IntegerLiteral;

    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 1;
    assert ((IntegerLiteral)innerCall.getArgs()[1]).getValue() == 2;
  }
  

  @Test
  public final void freeFunctionCallsNested() {
    String putsOnePlusTwo = "puts(1 + puts(2))";
    SyntaxNode node =
        new Parser(new Tokenizer(putsOnePlusTwo)
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;
    assert "puts".equals(call.getName());
    assert call.getArgs().length == 1;
    assert call.getArgs()[0] instanceof FunctionCall;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[0];
    assert "+".equals(innerCall.getName());
    assert innerCall.getArgs().length == 2;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 1;
    assert innerCall.getArgs()[1] instanceof FunctionCall;

    FunctionCall innerPuts = (FunctionCall)innerCall.getArgs()[1];
    assert "puts".equals(innerPuts.getName());
    assert innerPuts.getArgs().length == 1;
    assert innerPuts.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerPuts.getArgs()[0]).getValue() == 2;
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
  public final void postfixFunctionCallWithOneParentheticalIntArg() {
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
  public final void postfixFunctionCallWithOneParentheticalArg() {
    SyntaxNode node =
        new Parser(new Tokenizer("11.+(stuff)").tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;

    assert "+".equals(call.getName());
    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert call.getArgs()[1] instanceof Variable;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 11;
    assert "stuff".equals(((Variable) call.getArgs()[1]).getName());
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
        new Parser(new Tokenizer("(11 +\n\n \n 4)")
            .tokenize())
            .parse();

    System.out.println(node);

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

  @Test
  public final void simpleFunctionDefinition() {
    SyntaxNode node =
        new Parser(new Tokenizer("def thunk:\n    58 + 2.flip \n")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "thunk".equals(def.getName());
    assert def.getArgs().length == 0;

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;
  }

  @Test
  public final void simpleFunctionDefinitionWithParenArglist() {
    SyntaxNode node =
        new Parser(new Tokenizer("def thunk():\n    58 + 2.flip \n")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "thunk".equals(def.getName());
    assert def.getArgs().length == 0;

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;
  }

  @Test
  public final void functionDefinitionWithOneArg() {
    SyntaxNode node =
        new Parser(new Tokenizer("def meth(arg):\n    58 + 2.flip \n")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "meth".equals(def.getName());
    assert def.getArgs().length == 1;
    assert "arg".equals(def.getArgs()[0].getName());

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;
  }

  @Test
  public final void functionDefinitionWithManyArgs() {
    SyntaxNode node =
        new Parser(new Tokenizer("def meth(arg, a2, a3):\n    58 + 2.flip \n")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "meth".equals(def.getName());
    assert def.getArgs().length == 3;
    assert "arg".equals(def.getArgs()[0].getName());
    assert "a2".equals(def.getArgs()[1].getName());
    assert "a3".equals(def.getArgs()[2].getName());

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;
  }

  @Test
  public final void functionDefinitionWithOneTypedArg() {
    SyntaxNode node =
        new Parser(new Tokenizer("def meth(String name):\n    58 + 2.flip \n")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "meth".equals(def.getName());
    assert def.getArgs().length == 1;
    assert "name".equals(def.getArgs()[0].getName());
    assert "String".equals(def.getArgs()[0].getType());

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;
  }

  @Test
  public final void functionDefinitionWithManyTypedArgs() {
    SyntaxNode node =
        new Parser(new Tokenizer("def meth(String name, Int age, Phone num):\n    58 + 2.flip \n")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "meth".equals(def.getName());
    assert def.getArgs().length == 3;
    assert "name".equals(def.getArgs()[0].getName());
    assert "String".equals(def.getArgs()[0].getType());

    assert "age".equals(def.getArgs()[1].getName());
    assert "Int".equals(def.getArgs()[1].getType());

    assert "num".equals(def.getArgs()[2].getName());
    assert "Phone".equals(def.getArgs()[2].getType());

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;
  }
}
