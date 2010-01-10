package purple;

import org.junit.Test;
import purple.syntax.Parser;
import purple.syntax.model.*;

import java.util.Arrays;

/**
 * Tests some more advanced parsing/model generation cases to do
 * with function declarations.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class FunctionParsingTest {
  @Test
  public final void functionDefinitionWithExplicitDoBlock() {
    SyntaxNode node =
        new Parser(new Tokenizer("def +(String name, Int age): {   58 + 2.flip }")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "+".equals(def.getName());
    assert def.getArgs().length == 2;

    assertArgument(def.getArgs()[0], "name", "String");
    assertArgument(def.getArgs()[1], "age", "Int");

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[1];
    assert "flip".equals(innerCall.getName());
    assert innerCall.getArgs().length == 1;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 2;
  }

  @Test
  public final void functionDefinitionWithExplicitDoBlockNewlines() {
    SyntaxNode node =
        new Parser(new Tokenizer("def +(String name, Int age): { \n  58 + 2.flip \n }")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "+".equals(def.getName());
    assert def.getArgs().length == 2;

    assertArgument(def.getArgs()[0], "name", "String");
    assertArgument(def.getArgs()[1], "age", "Int");

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[1];
    assert "flip".equals(innerCall.getName());
    assert innerCall.getArgs().length == 1;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 2;
  }

  @Test
  public final void functionDefinitionWithExplicitDoBlockNewlinesMessy() {
    SyntaxNode node =
        new Parser(new Tokenizer("def +(String name, Int age):\n\n { \n  58 + 2.flip \n }")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "+".equals(def.getName());
    assert def.getArgs().length == 2;

    assertArgument(def.getArgs()[0], "name", "String");
    assertArgument(def.getArgs()[1], "age", "Int");

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[1];
    assert "flip".equals(innerCall.getName());
    assert innerCall.getArgs().length == 1;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 2;
  }

  @Test
  public final void functionDefinitionWithExplicitDoBlockNewlinesAndGrouping() {
    SyntaxNode node =
        new Parser(new Tokenizer("def +(String name, Int age):\n\n { \n  (58 + 2.flip) \n }")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "+".equals(def.getName());
    assert def.getArgs().length == 2;

    assertArgument(def.getArgs()[0], "name", "String");
    assertArgument(def.getArgs()[1], "age", "Int");

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[1];
    assert "flip".equals(innerCall.getName());
    assert innerCall.getArgs().length == 1;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 2;
  }

  @Test
  public final void functionDefinitionWithExplicitDoBlockNewlinesAndMessyGrouping() {
    SyntaxNode node =
        new Parser(new Tokenizer("def +(String name, Int age):\n\n { \n  (58 + (2.flip)) \n }")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "+".equals(def.getName());
    assert def.getArgs().length == 2;

    assertArgument(def.getArgs()[0], "name", "String");
    assertArgument(def.getArgs()[1], "age", "Int");

    assert def.getBody() instanceof FunctionCall;
    FunctionCall call = (FunctionCall)def.getBody();

    assert call.getArgs().length == 2;
    assert call.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral) call.getArgs()[0]).getValue() == 58;
    assert call.getArgs()[1] instanceof FunctionCall;
    
    FunctionCall innerCall = (FunctionCall) call.getArgs()[1];
    assert "flip".equals(innerCall.getName());
    assert innerCall.getArgs().length == 1;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 2;
  }

  @Test
  public final void multilineFunctionDef() {
    SyntaxNode node =
        new Parser(new Tokenizer("def +(age):\n\n { \n  58 + 2.flip \n 4 }")
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionDef;
    FunctionDef def = (FunctionDef) node;

    assert "+".equals(def.getName());
    assert def.getArgs().length == 1;

    assertArgument(def.getArgs()[0], "age", "unknown");

    assert def.getBody() instanceof FunctionCall;
    assert ((FunctionCall) def.getBody()).isDoBlock();
    DoBlock block = (DoBlock)def.getBody();

    assert block.getArgs().length == 0;
    assert block.getSequence().length == 2;

    assert block.getSequence()[0] instanceof FunctionCall;
    FunctionCall call1 = (FunctionCall) block.getSequence()[0];
    assert ((IntegerLiteral) call1.getArgs()[0]).getValue() == 58;  

    FunctionCall innerCall = (FunctionCall) call1.getArgs()[1];
    assert "flip".equals(innerCall.getName());
    assert innerCall.getArgs().length == 1;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 2;
  }

  @Test
  public final void groupedFreeFunctionCall() {
    String putsOnePlusTwo = "puts 1 + (33 - 2)";
    SyntaxNode node =
        new Parser(new Tokenizer(putsOnePlusTwo)
            .tokenize()).parse();

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;
    assert "puts".equals(call.getName());
    
    assert !call.isDoBlock();
    assert call.getArgs().length == 1;
    assert call.getArgs()[0] instanceof FunctionCall;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[0];
    assert "+".equals(innerCall.getName());
    assert innerCall.getArgs().length == 2;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert innerCall.getArgs()[1] instanceof FunctionCall;

    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 1;

    FunctionCall groupInner = (FunctionCall) innerCall.getArgs()[1];
    assert "-".equals(groupInner.getName());
    assert groupInner.getArgs()[0] instanceof IntegerLiteral;
    assert groupInner.getArgs()[1] instanceof IntegerLiteral;

    assert ((IntegerLiteral)groupInner.getArgs()[0]).getValue() == 33;
    assert ((IntegerLiteral)groupInner.getArgs()[1]).getValue() == 2;
  }

  @Test
  public final void freeFunctionCallInFunction() {
    String putsOnePlusTwo = "def hi: puts 1 + (33 - 2)";
    SyntaxNode node =
        new Parser(new Tokenizer(putsOnePlusTwo)
            .tokenize()).parse();

    System.out.println(node);

    assert node instanceof FunctionCall;
    FunctionCall call = (FunctionCall) node;
    assert "puts".equals(call.getName());

    assert !call.isDoBlock();
    assert call.getArgs().length == 1;
    assert call.getArgs()[0] instanceof FunctionCall;

    FunctionCall innerCall = (FunctionCall) call.getArgs()[0];
    assert "+".equals(innerCall.getName());
    assert innerCall.getArgs().length == 2;
    assert innerCall.getArgs()[0] instanceof IntegerLiteral;
    assert innerCall.getArgs()[1] instanceof FunctionCall;

    assert ((IntegerLiteral)innerCall.getArgs()[0]).getValue() == 1;

    FunctionCall groupInner = (FunctionCall) innerCall.getArgs()[1];
    assert "-".equals(groupInner.getName());
    assert groupInner.getArgs()[0] instanceof IntegerLiteral;
    assert groupInner.getArgs()[1] instanceof IntegerLiteral;

    assert ((IntegerLiteral)groupInner.getArgs()[0]).getValue() == 33;
    assert ((IntegerLiteral)groupInner.getArgs()[1]).getValue() == 2;
  }

  private static void assertArgument(Argument argument, String argName, String argType) {
    assert argName.equals(argument.getName());
    assert argType.equals(argument.getType());
  }
}