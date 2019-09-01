package nz.ac.vuw.ecs.msr19;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

import static nz.ac.vuw.ecs.msr19.Commons.*;

public class ComputeTagCooccurence {

    public static void main(String[] args) throws Exception {
        long t1 = System.currentTimeMillis();

        precondition(args.length>0,"the program needs one parameter - the location (folder) of the input CSV file UserTags.csv ");

        File dir = new File(args[0]);
        precondition(dir.exists(),"Input file does not exit: " + dir.getAbsolutePath());
        precondition(dir.isDirectory(),"Input file is not a folder: " + dir);

        File file = new File(dir,"UserTags.csv");
        precondition(file.exists(),"UserTags file does not exit: " + file.getAbsolutePath());

        Map<Integer, EnumSet<PLTag>> tagsBySnippetId = importUserPLTags(file);
        Map<Pair,Integer> pairCounts = computePairCounts(tagsBySnippetId);

        // reporting
        File resultFile = new File("tag-cooccurence.csv");
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
