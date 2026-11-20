import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Scanner;

// hash_app - simple hash bucket system that stores strings in files
// usage: hash_app <n> <s>
//   n = number of buckets (files 1.txt ... n.txt)
//   s = max number of characters in one bucket file
//
// Overflow: when a bucket file is full we make a new file for the same bucket:
//   3.txt -> 3_1.txt -> 3_2.txt -> ...
// The next file in the chain is found by its name, so nothing else is stored in the files.
public class HashApp {

    // FNV-1a 32 bit hash
    static long fnv1a(String text) {
        long h = 2166136261L;                       // starting value (offset basis)
        byte[] bytes = text.getBytes(StandardCharsets.UTF_8);
        for (byte b : bytes) {
            h = h ^ (b & 0xff);                     // xor with the byte
            h = (h * 16777619L) & 0xFFFFFFFFL;      // multiply by FNV prime, keep 32 bits
        }
        return h;
    }

    // which bucket (1..n) the text goes to
    static int getBucket(String text, int n) {
        return (int) (fnv1a(text) % n) + 1;
    }

    // name of a file in the chain of a bucket
    // number 0 = main file (3.txt), number 1 = first overflow file (3_1.txt), ...
    static String chainName(int bucket, int number) {
        if (number == 0) {
            return bucket + ".txt";
        }
        return bucket + "_" + number + ".txt";
    }

    // go through the chain and return the number of the LAST file that exists
    static int findLastFile(int bucket) {
        int number = 0;
        while (new File(chainName(bucket, number + 1)).exists()) {
            number++;
        }
        return number;
    }

    // how many characters are in the file (0 if the file does not exist yet)
    static int fileLength(String filename) throws IOException {
        File file = new File(filename);
        if (!file.exists()) {
            return 0;
        }
        return Files.readString(file.toPath(), StandardCharsets.UTF_8).length();
    }

    // add the text to the last file of its bucket, or to a new overflow file
    static String saveString(String text, int n, int s) throws IOException {
        int bucket = getBucket(text, n);
        int last = findLastFile(bucket);
        String filename = chainName(bucket, last);
        int current = fileLength(filename);

        // if the text does not fit, make a new overflow file
        // (if the file is empty we write anyway, so a very long string cannot get stuck)
        if (current > 0 && current + text.length() > s) {
            last++;
            filename = chainName(bucket, last);
        }

        try (FileWriter writer = new FileWriter(filename, true)) {   // true = append
            writer.write(text);
        }
        return filename;
    }

    public static void main(String[] args) {
        // check the command line arguments
        if (args.length != 2) {
            System.out.println("Usage: hash_app <n> <s>");
            return;
        }
        int n, s;
        try {
            n = Integer.parseInt(args[0]);
            s = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.out.println("n and s must be numbers");
            return;
        }
        if (n < 1 || s < 1) {
            System.out.println("n and s must be at least 1");
            return;
        }

        // ask for strings until the user presses Ctrl+C
        // the scanner is closed automatically at the end of the try block
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.print("Please enter the string: ");
                if (!scanner.hasNextLine()) {
                    break;                              // end of input
                }
                String text = scanner.nextLine();
                if (text.isEmpty()) {
                    continue;                           // ignore empty lines
                }
                try {
                    String filename = saveString(text, n, s);
                    System.out.println(text + " added to " + filename);
                } catch (IOException e) {
                    System.out.println("Could not write to file: " + e.getMessage());
                }
            }
        }
    }
}
