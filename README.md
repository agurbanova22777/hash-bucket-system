# hash_app

A simple file-based hash bucket system written in Java.
The program reads strings from the keyboard, hashes each one and stores it in one of `n` bucket files (`1.txt` ... `n.txt`).
If a bucket file gets too big (more than `s` characters), the program continues in overflow files.

## How to compile and run

You need Java (JDK) installed.

```bash
javac HashApp.java        # compile (the launcher also does this automatically if needed)
./hash_app 5 20           # run: n = 5 buckets, s = 20 characters per file
```

On Windows (cmd or PowerShell) use `hash_app.cmd 5 20`. In Git Bash `./hash_app 5 20` works too.

Arguments:

| Argument | Meaning |
|----------|---------|
| `n` | number of hash buckets (files `1.txt` to `n.txt`) |
| `s` | maximum number of characters in one bucket file |

The program keeps asking for strings until you press **Ctrl+C**. Empty lines are ignored.
The bucket files are created in the folder where you run the program.

Example:

```
$ ./hash_app 5 20
Please enter the string: Hello
Hello added to 2.txt
Please enter the string: World
World added to 1.txt
Please enter the string: Test
Test added to 5.txt
```

## Hashing algorithm

I used **FNV-1a (32-bit)**, a fast and simple non-cryptographic hash function. I implemented it myself in `HashApp.java`.

1. Start with the value `2166136261` (offset basis).
2. For every byte of the string (UTF-8): XOR the byte into the hash, then multiply the hash by `16777619` (FNV prime) and keep only 32 bits.
3. The bucket number is `hash % n + 1`, so it is always between `1` and `n`.

The same string always gives the same bucket, and different strings can collide (end up in the same file), which is allowed.

## Overflow bucket reference method

Every bucket is a **chain of files** and the reference between them is the **file name**:

```
3.txt  ->  3_1.txt  ->  3_2.txt  ->  3_3.txt ...
(main)     (overflow 1) (overflow 2) (overflow 3)
```

- The main file of bucket `b` is `b.txt`. Overflow files are `b_1.txt`, `b_2.txt`, `b_3.txt`, and so on.
- To save a string, the program starts at `b.txt` and checks if `b_1.txt` exists, then `b_2.txt`, and so on, until the next file does not exist. The last existing file is the one we write to, so the overflow file is never confused with the first file.
- If the string fits in the last file (current characters + length of the string <= `s`), it is added to the end of that file.
- If it does not fit, a new overflow file `b_(k+1).txt` is created and the string is written there.
- Nothing except the strings is written inside the files, and the strings are not separated by anything (for example `HelloWorldTest`).
- A string is never split between two files. If one string is longer than `s`, it is written alone in a new file (an empty file always accepts it), so this is the only case when a file can have more than `s` characters.
- Because the chain is found from the file names, the program can be stopped and started again and it continues in the correct last file.

Example with `n = 1` and `s = 10`, entering `Hello` five times:

```
1.txt   -> HelloHello
1_1.txt -> HelloHello
1_2.txt -> Hello
```

## Files

| File | Description |
|------|-------------|
| `HashApp.java` | the whole program |
| `hash_app` | launcher script for Linux / macOS / Git Bash |
| `hash_app.cmd` | launcher for Windows |
| `.gitignore` | ignores the generated `.txt` bucket files and `.class` files |
| `.gitattributes` | keeps correct line endings for the launcher scripts |