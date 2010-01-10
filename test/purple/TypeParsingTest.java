package purple;

import org.junit.Test;
import purple.syntax.Parser;
import purple.syntax.model.*;

import java.util.List;

/**
 * Tests type declarations.
 *
 * @author Dhanji R. Prasanna (dhanji@gmail com)
 */
public class TypeParsingTest {
  @Test
  public final void classDecl() {
    String expression = "class Person: {\n String name \n Int age \n}";
    List<Token> tokens = new Tokenizer(expression)
        .tokenize();
    SyntaxNode node =
        new Parser(tokens).parse();

    System.out.println(new Stringizer().detokenize(tokens));

    System.out.println(node);

    assert node instanceof ClassDef;
    assert "Person".equals(((ClassDef) node).getName());

    List<FieldDef> fields = ((ClassDef) node).getFields();
    assert fields.size() == 2;
    assert "name".equals(fields.get(0).getName());
    assert "age".equals(fields.get(1).getName()); 

  }

  @Test
  public final void mixedClassAndFunctionDefs() {
    String expression = "class Person: {\n String name \n Int age \n} \n def +(a, b): a + b";
    List<Token> tokens = new Tokenizer(expression)
        .tokenize();
    SyntaxNode node = new Parser(tokens).parse();

    System.out.println(new Stringizer().detokenize(tokens));

    System.out.println(node);

    assert node instanceof ClassDef;
    assert "Person".equals(((ClassDef) node).getName());

    List<FieldDef> fields = ((ClassDef) node).getFields();
    assert fields.size() == 2;
    assert "name".equals(fields.get(0).getName());
    assert "age".equals(fields.get(1).getName());

  }
}