package nz.ac.vuw.ecs.msr19;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import static nz.ac.vuw.ecs.msr19.Commons.*;


public class CountTags {

    public static void main(String[] args) throws Exception {

        long t1 = System.currentTimeMillis();
        precondition(args.length>0,"the program needs one parameter - the location (folder) of the input CSV files UserTags.csv and LinguistTags.csv ");

        File dir = new File(args[0]);
        precondition(dir.exists(),"Input file does not exit: " + dir.getAbsolutePath());
        precondition(dir.isDirectory(),"Input file is not a folder: " + dir);

        System.out.println("!!! Start program with 15G heap or more for best performance, use the following parameter: -Xmx15g");

        // parsing  tags
        File tagFile = new File(dir,"UserTags.csv");
        precondition(tagFile.exists(),"Tag file does not exit: " + tagFile.getAbsolutePath());

        Map<Integer, EnumSet<PLTag>> plTagsBySnippetId = importUserPLTags(tagFile);
        Map<Integer,AtomicInteger> countsByNumberOfTags = new ConcurrentHashMap<>();

        plTagsBySnippetId.keySet().parallelStream()
            .forEach(id -> {
                EnumSet<PLTag> tags = plTagsBySnippetId.get(id);
                int size = tags.size();
                countsByNumberOfTags.compute(size,(k,v) -> {
                    if (v==null) {
                        return new AtomicInteger(1);
                    }
                    else {
                        v.incrementAndGet();
                        return v;
                    }
                });
            });


        System.out.println("Number of snippets by number of tags (format: tagcount \\t numberofsnippets )");
        countsByNumberOfTags.keySet().stream()
            .sorted()
            .forEach(
                tagCount -> {
                    System.out.println(tagCount+"\t"+countsByNumberOfTags.get(tagCount));
                }
            );

        long t2 = System.currentTimeMillis();
        System.out.println("Script runtime: " + (t2-t1) + "ms");

    }


}
