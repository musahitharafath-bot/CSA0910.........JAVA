import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

/** Q5: student-derived ranking design: generics, bounded types, wildcards, and tests that break faulty comparators. */
class RankedBoard<T> {
    private final List<T> items = new ArrayList<>();
    private final Comparator<? super T> order;            // consumer of T: ? super T

    RankedBoard(Comparator<? super T> order) { this.order = Objects.requireNonNull(order); }
    void add(T item) { items.add(item); }
    void addAll(Collection<? extends T> more) { items.addAll(more); }   // producer of T: ? extends T
    List<T> ranking() { List<T> c = new ArrayList<>(items); c.sort(order); return c; }
    List<T> top(int k) { List<T> r = ranking(); return new ArrayList<>(r.subList(0, Math.min(k, r.size()))); }

    /** Competition ranks (1, 2, 2, 4 ...): items that compare equal share a rank. */
    List<Integer> ranks() {
        List<T> r = ranking(); List<Integer> out = new ArrayList<>();
        for (int i = 0; i < r.size(); i++) out.add(i > 0 && order.compare(r.get(i - 1), r.get(i)) == 0 ? out.get(i - 1) : i + 1);
        return out;
    }

    /** Bounded type: only types that know their own natural order may use this factory. */
    static <E extends Comparable<? super E>> RankedBoard<E> natural() { return new RankedBoard<>(Comparator.naturalOrder()); }
}

/** Design option A: the order is part of the class (Comparable). */
class ComparableTeam implements Comparable<ComparableTeam> {
    final String name; final int points, goalDiff;
    ComparableTeam(String n, int p, int g) { name = n; points = p; goalDiff = g; }
    @Override public int compareTo(ComparableTeam o) {
        int c = Integer.compare(o.points, points);
        if (c != 0) return c;
        c = Integer.compare(o.goalDiff, goalDiff);
        return c != 0 ? c : name.compareTo(o.name);
    }
    @Override public String toString() { return name + "(" + points + "," + goalDiff + ")"; }
}

record Player(String name, int goals, int assists) { }
record College(String name, int gold, int silver) { }

public class RankingDesign {
    static List<Team> load(String file) throws Exception {
        List<Team> l = new ArrayList<>();
        for (String s : Files.readAllLines(Path.of(file)).subList(1, Files.readAllLines(Path.of(file)).size())) {
            String[] p = s.split(","); l.add(new Team(p[0], Integer.parseInt(p[1]), Integer.parseInt(p[2])));
        }
        return l;
    }

    /** Counts violations of the three laws that a consistent comparator must satisfy. */
    static <T> String laws(Comparator<? super T> c, List<T> xs) {
        int refl = 0, anti = 0, trans = 0;
        for (T a : xs) if (c.compare(a, a) != 0) refl++;
        for (T a : xs) for (T b : xs) if (Integer.signum(c.compare(a, b)) != -Integer.signum(c.compare(b, a))) anti++;
        for (T a : xs) for (T b : xs) for (T d : xs) if (c.compare(a, b) <= 0 && c.compare(b, d) <= 0 && c.compare(a, d) > 0) trans++;
        return "reflexive violations " + refl + ", antisymmetry violations " + anti + ", transitivity violations " + trans;
    }

    static final Comparator<Team> GOOD = Comparator.comparingInt((Team t) -> t.points).reversed()
            .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed())
            .thenComparing(t -> t.name);
    static final Comparator<Team> SUBTRACT = (a, b) -> {
        int c = b.points - a.points; if (c != 0) return c;                 // overflow-prone
        c = b.goalDiff - a.goalDiff; return c != 0 ? c : a.name.compareTo(b.name);
    };
    static final Comparator<Team> NO_TIEBREAK = (a, b) -> Integer.compare(b.points, a.points);   // ignores goal difference and name
    static final Comparator<Team> TOLERANT = (a, b) -> Math.abs(a.points - b.points) <= 1 ? 0 : Integer.compare(b.points, a.points);

    static String names(List<Team> l) { StringBuilder sb = new StringBuilder("["); for (Team t : l) sb.append(t).append(' '); return sb.toString().trim() + "]"; }

    public static void main(String[] args) throws Exception {
        List<Team> ties = load("input/teams_ties.csv");

        System.out.println("=== Design A: Comparable vs design B: separate Comparators");
        RankedBoard<ComparableTeam> natural = RankedBoard.natural();
        RankedBoard<Team> chain = new RankedBoard<>(GOOD);
        for (Team t : ties) { natural.add(new ComparableTeam(t.name, t.points, t.goalDiff)); chain.add(t); }
        System.out.println("Comparator chain : " + chain.ranking());
        System.out.println("Comparable       : " + natural.ranking());
        System.out.println("Same result?       " + chain.ranking().toString().equals(natural.ranking().toString()));
        RankedBoard<Team> shared = new RankedBoard<>(Comparator.comparingInt((Team t) -> t.points).reversed()
                .thenComparing(Comparator.comparingInt((Team t) -> t.goalDiff).reversed()));     // no name tie-break: equal items share a rank
        shared.addAll(ties);
        System.out.println("Competition ranks without the name tie-break (equal items compare as equal): " + shared.ranks());
        RankedBoard<Team> alt = new RankedBoard<>(Comparator.comparing((Team t) -> t.name));
        alt.addAll(ties);
        System.out.println("Same data, order by name only (another Comparator, no change to Team): " + alt.top(3));

        System.out.println("\n=== Wildcards and reuse for other types");
        RankedBoard<Player> scorers = new RankedBoard<>(Comparator.comparingInt(Player::goals).reversed().thenComparing(Comparator.comparingInt(Player::assists).reversed()).thenComparing(Player::name));
        scorers.addAll(List.of(new Player("Ravi", 7, 2), new Player("Kiran", 7, 4), new Player("Asha", 9, 1)));
        System.out.println("Players  : " + scorers.ranking());
        RankedBoard<College> colleges = new RankedBoard<>(Comparator.comparingInt(College::gold).reversed().thenComparing(Comparator.comparingInt(College::silver).reversed()).thenComparing(College::name));
        colleges.addAll(List.of(new College("SIMATS", 5, 2), new College("ABC", 5, 3), new College("XYZ", 2, 9)));
        System.out.println("Colleges : " + colleges.ranking());
        Comparator<Object> byText = Comparator.comparing(Object::toString);            // Comparator<Object> works for Team: ? super T
        RankedBoard<Team> viaSuper = new RankedBoard<>(byText);
        viaSuper.addAll(ties);
        System.out.println("Comparator<Object> accepted by RankedBoard<Team>: " + viaSuper.top(2));

        System.out.println("\n=== Test data 1: ties at every level (12 teams)");
        System.out.println("Expected by hand: Sharks, Tigers (12,10 -> name), Lions(12,7), Eagles(9,6), Bisons/Panthers (9,4 -> name), Cobras/Falcons/Hawks (6,1 -> name), Rhinos(6,-2), Jaguars/Wolves (3,0 -> name)");
        System.out.println("Actual   : " + chain.ranking());
        System.out.println("Laws for GOOD comparator : " + laws(GOOD, ties));
        List<Team> shuffled = new ArrayList<>(ties);
        Collections.shuffle(shuffled, new Random(5));
        System.out.println("Ranking independent of input order? " + names(shuffled.stream().sorted(GOOD).toList()).equals(names(ties.stream().sorted(GOOD).toList())));

        System.out.println("\n=== Test data 2: faulty comparators");
        System.out.println("-- (a) NO_TIEBREAK ignores goal difference and name; result depends on input order (stable sort)");
        List<Team> rev = new ArrayList<>(ties); Collections.reverse(rev);
        System.out.println("   input order 1 -> " + names(ties.stream().sorted(NO_TIEBREAK).toList()));
        System.out.println("   input order 2 -> " + names(rev.stream().sorted(NO_TIEBREAK).toList()));
        System.out.println("   rankings equal? " + names(ties.stream().sorted(NO_TIEBREAK).toList()).equals(names(rev.stream().sorted(NO_TIEBREAK).toList())));
        System.out.println("   " + laws(NO_TIEBREAK, ties) + "  (consistent, but ties are not broken as required)");

        List<Team> ext = load("input/teams_extreme.csv");
        System.out.println("-- (b) SUBTRACT uses b.points - a.points; overflows for values of opposite sign near the int limits");
        System.out.println("   correct   : " + names(ext.stream().sorted(GOOD).toList()));
        System.out.println("   subtract  : " + names(ext.stream().sorted(SUBTRACT).toList()));
        System.out.println("   " + laws(SUBTRACT, ext));
        Team big = ext.get(0), small = ext.get(1);
        System.out.println("   compare(Alpha 2e9, Bravo -2e9): GOOD = " + GOOD.compare(big, small) + ", SUBTRACT = " + SUBTRACT.compare(big, small) + "  (Alpha must rank first, i.e. negative)");

        System.out.println("-- (c) TOLERANT treats points within 1 as equal; equality is not transitive");
        List<Team> three = List.of(new Team("A", 9, 0), new Team("B", 10, 0), new Team("C", 11, 0));
        System.out.println("   " + laws(TOLERANT, three));
        Random r = new Random(1); List<Team> many = new ArrayList<>();
        for (int i = 0; i < 400; i++) many.add(new Team("T" + i, r.nextInt(30), 0));
        try { many.sort(TOLERANT); System.out.println("   sorting 400 random teams: no exception (result silently unreliable)"); }
        catch (IllegalArgumentException e) { System.out.println("   sorting 400 random teams: IllegalArgumentException: " + e.getMessage()); }
        System.out.println("   TOLERANT ranking of A(9) B(10) C(11): " + names(three.stream().sorted(TOLERANT).toList()) + " and reversed input: " + names(new ArrayList<>(List.of(three.get(2), three.get(1), three.get(0))).stream().sorted(TOLERANT).toList()));
    }
}
