package nz.ac.vuw.ecs.msr19;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import static nz.ac.vuw.ecs.msr19.Commons.*;

public class ComputeAlphas {

    public static void main(String[] args) throws Exception {
        long t1 = System.currentTimeMillis();
        precondition(args.length>0,"the program needs one parameter - the location (folder) of the input CSV files UserTags.csv and LinguistTags.csv ");

        File dir = new File(args[0]);
        precondition(dir.exists(),"Input file does not exit: " + dir.getAbsolutePath());
        precondition(dir.isDirectory(),"Input file is not a folder: " + dir);

        Map<Integer,Integer> alphas = computeAlphas(dir);

        // reporting
        File resultFile = new File("alphas.csv");
        try (PrintWriter out = new PrintWriter(new FileWriter(resultFile))) {
            System.out.println("Alphas by k (format: k tab alpha)");
            ((ConcurrentHashMap<Integer, Integer>) alphas).keySet().stream()
                .sorted()
                .forEach(
                    k -> {
                        System.out.println("" + k + "\t" + alphas.get(k));
                        out.println("" + k + "\t" + alphas.get(k));
                    }
                );
            System.out.println();
        }

        System.out.println("Results written to " + resultFile.getAbsolutePath());
        long t2 = System.currentTimeMillis();
        System.out.println("Script runtime: " + (t2-t1) + "ms");

    }

    public static Map<Integer,Integer> computeAlphas(File dir) throws Exception {
        File tagFile = new File(dir,"UserTags.csv");
        File linguistFile = new File(dir,"LinguistTags.csv");
        precondition(tagFile.exists(),"Tag file does not exit: " + tagFile.getAbsolutePath());
        precondition(linguistFile.exists(),"Linguist file does not exit: " + linguistFile.getAbsolutePath());

        System.out.println("!!! Start program with 15G heap or more for best performance, use the following parameter: -Xmx15g");

        // parsing  tags
        Map<Integer, EnumSet<PLTag>> plTagsBySnippetId = importUserPLTags(tagFile);
        Map<Integer, List<PLTag>> linguistTagsBySnippetId = importLinguistTags(linguistFile);

        // compute match count by k
        AtomicInteger counter = new AtomicInteger(0);
        Map<Integer,Integer> matches = new ConcurrentHashMap<>(); // key -1 = means no match found
        plTagsBySnippetId.keySet().parallelStream()
            .forEach(snippetId -> {
                int c = counter.incrementAndGet();
                if (c%1_000_000==0) {
                    System.out.println("matching snippet #" + c);
                }
                Set<PLTag> tags = plTagsBySnippetId.get(snippetId);
                List<PLTag> linguistTags = linguistTagsBySnippetId.get(snippetId);
                boolean matched = false;
                if (linguistTags!=null) {
                    for (int i = 0; i < linguistTags.size(); i++) {
                        if (tags.contains(linguistTags.get(i))) {
                            // match !
                            matches.compute(i + 1, (k, v) -> v == null ? 1 : v + 1);
                            matched = true;
                            break;
                        }
                    }
                }
                if (!matched) {
                    matches.compute(-1,(k,v) -> v==null?1:v+1);
                }
            });
        return matches;
    }

}
