package nz.ac.vuw.ecs.msr19;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Commons {

    public static final class Pair implements Comparable<Pair>{
        PLTag left = null;
        PLTag right = null;

        public Pair(PLTag left, PLTag right) {
            this.left = left;
            this.right = right;
        }

        @Override
        public int compareTo(Pair o) {
            int r = this.left.compareTo(o.left);
            if (r!=0) {
                return r;
            }
            else {
                return this.right.compareTo(o.right);
            }
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Pair pair = (Pair) o;
            return Objects.equals(left, pair.left) &&
                    Objects.equals(right, pair.right);
        }

        @Override
        public int hashCode() {
            return Objects.hash(left, right);
        }

        @Override
        public String toString() {
            return "Pair{" +
                    "left='" + left + '\'' +
                    ", right='" + right + '\'' +
                    '}';
        }
    }

    public static final Set<String> PL_TAGS = EnumSet.allOf(PLTag.class).stream().map(t -> t.toString()).collect(Collectors.toSet());
    static {
        assert PL_TAGS.size()==17;
    }

    public static void precondition(boolean check,String message) {
        if (!check) {
            throw new IllegalStateException(message);
        }
    }

    public static EnumSet<PLTag> parsePLTags(String tagListDef) {

        Set<PLTag> set = Stream.of(tagListDef.split("><"))
            .map(t -> t.replace("<",""))
            .map(t -> t.replace(">",""))
            .map(t -> t.replace(" ",""))
            .map(t -> t.toLowerCase())
            .map(t -> removeVersionInfo(t))
            // TODO: more magic conversions
            .filter(t -> isPlTag(t))
            .map(t -> PLTag.from(t))
            .collect(Collectors.toSet());

        return set.isEmpty()?EnumSet.noneOf(PLTag.class):EnumSet.copyOf(set);
    }

    public static String removeVersionInfo(String t) {
        for (int i=0;i<t.length();i++) {
            if (t.charAt(i)=='-' && i<t.length()-1 && Character.isDigit(t.charAt(i+1))) {
                return t.substring(0,i);
            }
        }
        return t;
    }

    public static boolean isPlTag(String t) {
        return PL_TAGS.contains(t);
    }

    public static Collection<Pair> findAllPairs(List<PLTag> list,int threshold) {
        Collection<Pair> pairs = new HashSet<>();
        int MAX = Math.min(list.size(),threshold);
        for (int i=0;i<MAX;i++) {
            for (int j=i+1;j<MAX;j++) {
                PLTag s1 = list.get(i);
                PLTag s2 = list.get(j);
                assert !s1.equals(s2);  // specific constraint, list allow duplicates
                if (s1.compareTo(s2)>0) {
                    PLTag _s = s2;
                    s2 = s1;
                    s1 = _s;
                }
                pairs.add(new Pair(s1,s2));
            }
        }
        return pairs;
    }

    public static Collection<Pair> findAllPairs(Collection<PLTag> coll,int threshold) {
        return coll instanceof List ? findAllPairs((List)coll,threshold):findAllPairs(new ArrayList<>(coll),threshold);
    }

    public static Map<Integer,EnumSet<PLTag>> importUserPLTags (File file) throws Exception {
        precondition(file.exists(),"Input file missing: " + file.getAbsolutePath());
        AtomicInteger counter = new AtomicInteger();
        Map<Integer, EnumSet<PLTag>> plTagsBySnippetId = new ConcurrentHashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
            stream.parallel()
                    .map(line -> {
                        String[] tokens = line.split(",");
                        assert tokens.length == 3;
                        int c = counter.incrementAndGet();
                        if (c%1_000_000==0) {
                            System.out.println("parsing and processing tag record #" + c);
                        }
                        return tokens;
                    })
                    .forEach(tokens -> {
                        String rawTags = tokens[2]; // last column
                        EnumSet<PLTag> plTags = parsePLTags(rawTags);
                        if (plTags.size()>0) { // only register snippets with tags
                            plTagsBySnippetId.put(Integer.parseInt(tokens[1]), plTags);
                        }
                    });
            System.out.println("Parsed user pl tags");
        }
        return plTagsBySnippetId;
    }

    // returns a map associating snippet ids wth linguist tags
    public static Map<Integer,List<PLTag>> importLinguistTags (File file) throws Exception {
        precondition(file.exists(),"Input file missing: " + file.getAbsolutePath());
        AtomicInteger counter = new AtomicInteger();
        Map<Integer, List<PLTag>> linguistTagsBySnippetId = new ConcurrentHashMap<>();
        try (Stream<String> stream = Files.lines(Paths.get(file.getAbsolutePath()))) {
            stream.parallel()
                    .map(line -> line.replace("\"",""))
                    .map(line -> line.replace("[",""))
                    .map(line -> line.replace("]",""))
                    .map(line -> line.replace(" ",""))
                    .map(line -> line.toLowerCase())
                    .map(line -> {
                        String[] tokens = line.split("\t");
                        assert tokens.length == 3;
                        int c = counter.incrementAndGet();
                        if (c%1_000_000==0) {
                            System.out.println("parsing and processing linguist record #" + c);
                        }
                        return tokens;
                    })
                    .forEach(tokens -> {
                        String rawTags = tokens[2]; // last column
                        List<PLTag> plTags = Stream.of(rawTags.split(","))
                                .map(t -> t.trim())
                                .filter(t -> isPlTag(t))
                                .map(t -> PLTag.from(t))
                                .collect(Collectors.toList());
                        linguistTagsBySnippetId.put(Integer.parseInt(tokens[1]),plTags);
                    });
            System.out.println("Parsed linguist tags");
        }
        return linguistTagsBySnippetId;
    }

    public static Map<Pair,Integer> computePairCounts(Map<Integer, ? extends Collection<PLTag>> tagsBySnippetId) {
        return computePairCounts(tagsBySnippetId,Integer.MAX_VALUE);
    }

    public static Map<Pair,Integer> computePairCounts(Map<Integer, ? extends Collection<PLTag>> tagsBySnippetId,int threshold) {
        Map<Pair,Integer> pairCounts = new ConcurrentHashMap<>();
        tagsBySnippetId.keySet().parallelStream().forEach(
                snippet -> {
                    Collection<PLTag> tags = tagsBySnippetId.get(snippet);
                    Collection<Pair> pairs = findAllPairs(tags,threshold);
                    for (Pair pair:pairs) {
                        pairCounts.compute(pair,(k,v) -> v==null?1:v+1);
                    }
                }
        );
        return pairCounts;
    }

}
