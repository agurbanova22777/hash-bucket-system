import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;

// hash_app - simple hash bucket system that stores strings in files
// usage: hash_app <n> <s>
//   n = number of buckets (files 1.txt ... n.txt)
//   s = max size of one bucket file
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

    // add the text to the end of its bucket file
    static String saveString(String text, int n, int s) throws IOException {
        String filename = getBucket(text, n) + ".txt";
        try (FileWriter writer = new FileWriter(filename, true)) {
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
        if (n < 1) {
            System.out.println("n must be at least 1");
            return;
        }

        // ask for strings until the user presses Ctrl+C
        Scanner scanner = new Scanner(System.in);
        while (true) {
            System.out.print("Please enter the string: ");
            if (!scanner.hasNextLine()) {
                break;
            }
            String text = scanner.nextLine();
            try {
                String filename = saveString(text, n, s);
                System.out.println(text + " added to " + filename);
            } catch (IOException e) {
                System.out.println("Could not write to file: " + e.getMessage());
            }
        }
    }
}
