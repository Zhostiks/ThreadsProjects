package concurrentHashMap;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * This program demonstrates the use of parallel hash mappings
 */

public class CHMDemo
{
    public static ConcurrentHashMap<String, Long> map = new ConcurrentHashMap<>();


    /**
     * enters all words from a given file
     * into a parallel hash mapping
     * @param file Given file
     */
    public static void process (Path file)
    {
        try (var in = new Scanner(file))
        {
            while(in.hasNext())
            {
                String word = in.next();
                map.merge (word , 1L, Long::sum);
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    /**
     * returns all directories
     * spawned by a given directory
     * @param rootDir Root directory
     * @return Return all directories spawned
     *               by the root directory
     */
    public static Set<Path> descendants (Path rootDir)
        throws IOException
    {
        try(Stream<Path> entries = Files.walk(rootDir))
        {
            return entries.collect(Collectors.toSet());
        }
    }

    public static void main (String [] args)
            throws InterruptedException, ExecutionException, IOException
    {
        int processors = Runtime.getRuntime().availableProcessors();
        ExecutorService executor = Executors.newFixedThreadPool(processors);
        Path pathToRoot = Path.of(".");
        for(Path p : descendants(pathToRoot))
        {
            if(p.getFileName().toString().endsWith(".java"))
                executor.execute(() ->process(p));
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
        map.forEach((k,v) ->
        {
            if (v >= 10)
                System.out.println(k+ " occurs " + v + " times");
        });

    }
}
