package blockingQueue;

import java.io.*;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.*;

/**
 * demonstrates the use of a blocking queue 
 * to control multiple threads of execution
 * this program searches through all the files 
 * in a directory and its subdirectories, outputting
 * lines of code containing the specified keyword
 */

public class BlockingQueueTest
{
    private static final int FILE_QUEUE_SIZE = 10;
    private static final int SEARCH_THREADS = 100;
    private static final Path DUMMY = Path.of("");
    private static BlockingQueue<Path> queue = new ArrayBlockingQueue<>(FILE_QUEUE_SIZE);

    public static void main(String [] args)
    {
        try (var in = new Scanner(System.in))
        {
            System.out.print("Enter base directory " + "(e.g. /opt/jdk-21-src): ");
            String directory = in.nextLine();
            System.out.print("Enter keyword " + "(e.g. volatile): ");
            String keyword = in.nextLine();

            Runnable enumerator = () -> {
            try
            {
                enumerate(Path.of(directory));
                queue.put(DUMMY);
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            catch (InterruptedException e)
            {

            }
        };

            new Thread(enumerator).start();
            for (int i = 0; i <= SEARCH_THREADS; i++)
            {
                Runnable searcher = () ->
                {
                    try
                    {
                        var done = false;
                        while (!done)
                        {
                            Path file = queue.take();
                            if (file == DUMMY)
                            {
                                queue.put(file);
                                done = true;
                            }
                            else search (file,keyword);
                        }
                    }
                    catch (IOException e)
                    {
                        e.printStackTrace();
                    }
                    catch (InterruptedException e)
                    {

                    }
                };
                new Thread(searcher).start();
            }
            }
    }

    public static void enumerate(Path directory)
        throws IOException, InterruptedException
    {
        try(Stream<Path> children = Files.list(directory))
        {
            for (Path child : children.collect(Collectors.toList()))
            {
                if (Files.isDirectory(child))
                    enumerate(child);
                else queue.put(child);
            }
        }
    }

    public static void search(Path file, String keyword)
        throws IOException
    {
        try (var in = new Scanner(file, StandardCharsets.UTF_8))
        {
            int lineNumber = 0;
            while(in.hasNextLine())
            {
                lineNumber++;
                String line = in.nextLine();
                if(line.contains(keyword))
                    System.out.printf("%s:%d:%s%n" , file, lineNumber, line);
            }
        }
    }
}