package org.staging.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Random;

import static org.staging.tool.Tool.*;

/*
        // example how to create a file for insert scripts
        File f = makeFile("Test1");
        FileWriter fw = new FileWriter(f, true);
        fw.append("This is a test file.\n");
        fw.close();
 */

public class Generator {

    private final Random RANDOM = new Random(1);

    private final int USERS_COUNT = 500_000;
    private final int POSTS_COUNT = 500_000;
    private final int TAG_COUNT = 150_000;

    @FunctionalInterface
    interface IOAction {
        void run() throws IOException;
    }

    public void generate() throws IOException {
        measure("generateUsers", this::generateUsers);
//        measure("generatePosts", this::generatePosts);
//        measure("generateMediaForPost", this::generateMediaForPost);
//        measure("generateTags", this::generateTags);
//        measure("generatePostTags", this::generatePostTags);
//        measure("generateReactions", this::generateReactions);
    }


    private void measure(String label, IOAction action) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        System.out.println("\n[" + LocalTime.now().format(fmt) + "] Starting " + label + "...");

        long start = System.nanoTime();
        action.run();  // allowed to throw IOException
        long end = System.nanoTime();

        double seconds = (end - start) / 1_000_000_000.0;
        System.out.printf("[%s] %s finished in %.3f s%n",
                LocalTime.now().format(fmt), label, seconds);
    }


    private File makeFile(String fileName) {
        String projectDir = System.getProperty("user.dir");
        return new File(projectDir + "/out_files", fileName + ".sql");
    }

    private void generateUsers() throws IOException {
        int fileN = 1;
        File f = makeFile("Users" + fileN);
        FileWriter fw = new FileWriter(f, true);

        for (int i = 0; i < USERS_COUNT; i++) {
//            if (i != 0 && i % 2000 == 0) {
//                fileN++;
//                fw.append("commit;");
//                fw.close();
//                f = makeFile("Users" + fileN);
//                fw = new FileWriter(f, true);
//            }
            fw.append(generateUserInsert());
        }

        fw.close();
    }

    private void generatePosts() throws IOException {
        File f = makeFile("Posts");
        FileWriter fw = new FileWriter(f, true);

        int postsCount = (int) (POSTS_COUNT * 0.5);
        int replyCount = (int) (POSTS_COUNT * 0.3);

        for (int i = 0; i < POSTS_COUNT; i++) {
            if (i < postsCount) {
                fw.append(generatePostInsert(generateUserId(), 0));
            } else if (i < replyCount + postsCount) {
                fw.append(generatePostInsert(generateUserId(), generatePostId(postsCount, replyCount + postsCount)));
            } else {
                fw.append(generatePostInsert(generateUserId(), generatePostId(replyCount + postsCount, POSTS_COUNT)));
            }
        }

        fw.close();
    }

    private void generateMediaForPost() throws IOException {
        File f = makeFile("Media");
        FileWriter fw = new FileWriter(f, true);

        int mediaCount = (int) (POSTS_COUNT * 0.25);

        for (int i = 0; i < mediaCount; i++) {
            fw.append(generateMediaInsert(generatePostId(1, mediaCount)));
        }

        fw.close();
    }

    private void generateReactions() throws IOException {
        File f = makeFile("Reactions");

        try (BufferedWriter fw = new BufferedWriter(new FileWriter(f, true), 1 << 16)) {

            StringBuilder sb = new StringBuilder(1 << 18);

            long total = (long) POSTS_COUNT * USERS_COUNT;
            long processed = 0;
            long lastPrint = 0;

            for (int i = 1; i <= POSTS_COUNT; i++) {
                for (int j = 1; j <= USERS_COUNT; j++) {

                    if (RANDOM.nextDouble() < 0.5 && RANDOM.nextDouble() < 0.7) {
                        sb.append(generateReactionInsert(j, i));
                    }

                    if (sb.length() > 1_000_000) {
                        fw.write(sb.toString());
                        sb.setLength(0);
                    }

                    processed++;
                    if (processed - lastPrint >= total / 200) {
                        double percent = (processed * 100.0) / total;
                        System.out.printf("\rProgress: %.2f%% (%d/%d)", percent, i, POSTS_COUNT);
                        System.out.flush();
                        lastPrint = processed;
                    }
                }
            }

            if (!sb.isEmpty()) fw.write(sb.toString());
            System.out.println("\nDone!");
        }
    }

    private void generateTags() throws IOException {
        File f = makeFile("Tags");
        FileWriter fw = new FileWriter(f, true);

        for (int i = 0; i < TAG_COUNT; i++) {
            fw.append(generateTagInsert());
        }

        fw.close();
    }

    private void generatePostTags() throws IOException {
        File f = makeFile("PostTags");

        try (BufferedWriter fw = new BufferedWriter(new FileWriter(f, true), 1 << 16)) {

            StringBuilder sb = new StringBuilder(1 << 18);

            long total = (long) POSTS_COUNT * TAG_COUNT;
            long processed = 0;
            long lastPrint = 0;

            for (int i = 0; i < POSTS_COUNT; i++) {
                for (int j = 0; j < TAG_COUNT; j++) {

                    if (RANDOM.nextDouble() < 0.00001) {
                        sb.append(generatePostTagsInsert(i, j));
                    }

                    processed++;

                    if (sb.length() > 1_000_000) {
                        fw.write(sb.toString());
                        sb.setLength(0);
                    }

                    if (processed - lastPrint >= total / 200) {
                        double percent = (processed * 100.0) / total;
                        System.out.printf("\r[PostTags] %.2f%% (%d/%d posts)", percent, i + 1, POSTS_COUNT);
                        System.out.flush();
                        lastPrint = processed;
                    }
                }
            }

            if (!sb.isEmpty()) fw.write(sb.toString());

            System.out.println("\n[PostTags] Done!");
        }
    }

    private int generateUserId() {
        return RANDOM.nextInt(USERS_COUNT) + 1;
    }

    private int generatePostId(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
}
