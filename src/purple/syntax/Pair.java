package purple.syntax;

/**
 * An ordered pair.
 */
public class Pair<A, B> {
  public final A first;
  public final B second;

  private Pair(A first, B second) {
    this.first = first;
    this.second = second;
  }

  public static <A, B> Pair<A, B> of(A a, B b) {
    return new Pair<A,B>(a, b);
  }
}
