package nz.ac.vuw.ecs.msr19;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static nz.ac.vuw.ecs.msr19.Commons.*;

public class ComputeLinguistCooccurence {

    public static void main(String[] args) throws Exception {

        long t1 = System.currentTimeMillis();

        precondition(args.length>1,"the program needs two parameter - the location (folder) of the input CSV file LinguistTags.csv, and a k threshold ");

        File dir = new File(args[0]);
        precondition(dir.exists(),"Input file does not exit: " + dir.getAbsolutePath());
        precondition(dir.isDirectory(),"Input file is not a folder: " + dir);

        File file = new File(dir,"LinguistTags.csv");
        precondition(file.exists(),"Linguist file does not exit: " + file.getAbsolutePath());

        int threshold = Integer.parseInt(args[1]);

        Map<Integer, List<PLTag>> tagsBySnippetId = importLinguistTags(file);

        Map<Pair,Integer> pairCounts = computePairCounts(tagsBySnippetId,threshold);

        // reporting
        File resultFile = new File("linguist-cooccurence-" + threshold + ".csv");
        try (PrintWriter out = new PrintWriter(new FileWriter(resultFile))) {
            pairCounts.keySet().stream()
                .sorted()
                .forEach(
                    k -> {
                        out.println("" + k.left + "\t" + k.right + "\t" + pairCounts.get(k));
                    }
                );
            System.out.println();
        }

        System.out.println("Results written to " + resultFile.getAbsolutePath());

        long t2 = System.currentTimeMillis();
        System.out.println("Script runtime: " + (t2-t1) + "ms");

    }


}
