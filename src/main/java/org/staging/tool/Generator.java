package org.staging.tool;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

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

    private final int USERS_COUNT = 200_000;
    private final int POSTS_COUNT = 400_000;
    private final int TAG_COUNT = 1000;
    private final int POST_TAGS_MAX = 300_000;
    private final int REACTIONS_MAX = 400_000;
    private final int FOLLOWS_COUNT = 100_000;

    @FunctionalInterface
    interface IOAction {
        void run() throws IOException;
    }

    public void generate() throws IOException {
        measure("generateUsers", this::generateUsers);
        measure("generatePosts", this::generatePosts);
        measure("generateMediaForPost", this::generateMediaForPost);
        measure("generateTags", this::generateTags);
        measure("generatePostTags", this::generatePostTags);
        measure("generateReactions", this::generateReactions);
        measure("generateFollows", this::generateFollows);
    }

    private void measure(String label, IOAction action) throws IOException {
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm:ss");
        System.out.println("\n[" + LocalTime.now().format(fmt) + "] Starting " + label + "...");

        long start = System.nanoTime();
        action.run();
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
        File f = makeFile("Users");
        FileWriter fw = new FileWriter(f, true);

        for (int i = 0; i < USERS_COUNT; i++) {
            if (i != 0 && i % 10000 == 0) {
                fw.append("commit;");
            }

            if (i <= 1000) {
                fw.append(generateUserInsert(true));
            } else {
                fw.append(generateUserInsert(false));
            }

//            if (i == 0) {
//                break;
//            }
        }

        fw.close();
    }

    private void generatePosts() throws IOException {
        File f = makeFile("Posts1");
        FileWriter fw = new FileWriter(f, true);

        int fileN = 2;

        int postsCount = (int) (POSTS_COUNT * 0.5);
        int replyCount = (int) (POSTS_COUNT * 0.3);

        for (int i = 0; i < POSTS_COUNT; i++) {
            if (i != 0 && i % 200_000 == 0) {
                fw.close();
                f = makeFile("Posts" + fileN);
                fw = new FileWriter(f, true);
            }

            // TODO BAD GENERATION OF REPLIES
            if (i < postsCount) {
                fw.append(generatePostInsert(generateUserId(), 0));
            } else if (i < replyCount + postsCount) {
                fw.append(generatePostInsert(generateUserId(), generatePostId(postsCount, replyCount + postsCount)));
            } else {
                fw.append(generatePostInsert(generateUserId(), generatePostId(replyCount + postsCount, POSTS_COUNT)));
            }

            if (i != 0 && i % 10000 == 0) {
                fw.append("commit;");
            }
        }

        fw.close();
    }

    private void generateMediaForPost() throws IOException {
        File f = makeFile("Media");
        FileWriter fw = new FileWriter(f, true);

        int mediaCount = (int) (POSTS_COUNT * 0.25);

        for (int i = 0; i < mediaCount; i++) {

            if (i < 1000) {
                fw.append(generateMediaInsert(generatePostId(1, mediaCount), true));
            } else {
                fw.append(generateMediaInsert(generatePostId(1, mediaCount), false));
            }

            if (i != 0 && i % 10000 == 0) {
                fw.append("commit;");
            }
        }

        fw.close();
    }

    private void generateReactions() throws IOException {
        File f = makeFile("Reactions1");
        FileWriter fw = new FileWriter(f, true);

        long total = (long) POSTS_COUNT * USERS_COUNT;
        long processed = 0;
        long lastPrint = 0;
        long rows = 0;
        int fileN = 2;

        for (int i = 1; i <= POSTS_COUNT; i++) {
            for (int j = 1; j <= USERS_COUNT; j++) {

                if (rows != 0 && rows % 200_000 == 0) {
                    fw.flush();
                    fw.close();
                    f = makeFile("Reactions" + fileN);
                    fw = new FileWriter(f, true);
                }

                if (RANDOM.nextDouble() < 0.5 && RANDOM.nextDouble() < 0.7) {
                    fw.write(generateReactionInsert(j, i));
                    rows++;
                }

                if (rows != 0 && rows % 10000 == 0) {
                    fw.write("commit;");
                }

                processed++;
                if (processed - lastPrint >= total / 200) {
                    double percent = (processed * 100.0) / total;
                    System.out.printf("\rProgress: %.2f%% (%d/%d) ROWS [%d]", percent, i, POSTS_COUNT, rows);
                    System.out.flush();
                    lastPrint = processed;
                }

                if (rows >= REACTIONS_MAX) {
                    fw.flush();
                    fw.close();
                    System.out.println("\n[PostTags] Done!");
                    return;
                }
            }
        }

        fw.flush();
        fw.close();

        System.out.println("\nDone!");
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
        File f = makeFile("PostTags1");
        FileWriter fw = new FileWriter(f, true);

        long total = (long) POSTS_COUNT * TAG_COUNT;
        long processed = 0;
        long lastPrint = 0;
        long rows = 0;
        int fileN = 2;

        for (int i = 0; i < POSTS_COUNT; i++) {
            for (int j = 0; j < TAG_COUNT; j++) {

                if (rows != 0 && rows % 200_000 == 0) {
                    fw.flush();
                    fw.close();
                    f = makeFile("PostTags" + fileN);
                    fw = new FileWriter(f, true);
                }

                if (RANDOM.nextDouble() < 0.001) {
                    fw.write(generatePostTagsInsert(i, j));
                    rows++;
                }

                processed++;

                if (rows != 0 && rows % 10000 == 0) {
                    fw.write("commit;");
                }

                if (processed - lastPrint >= total / 200) {
                    double percent = (processed * 100.0) / total;
                    System.out.printf("\r[PostTags] %.2f%% (%d/%d posts) ROWS [%d]", percent, i + 1, POSTS_COUNT, rows);
                    System.out.flush();
                    lastPrint = processed;
                }

                if (rows >= POST_TAGS_MAX) {
                    fw.flush();
                    fw.close();
                    System.out.println("\n[PostTags] Done!");
                    return;
                }
            }
        }

        fw.flush();
        fw.close();

        System.out.println("\n[PostTags] Done!");
    }

    private void generateFollows() throws IOException {
        File f = makeFile("Follows");
        FileWriter fw = new FileWriter(f, true);

        Random r = new Random();
        Set<String> usedPairs = new HashSet<>();

        for (int i = 0; i < FOLLOWS_COUNT; i++) {
            int followerId;
            int followeeId;

            while (true) {
                followerId = r.nextInt(USERS_COUNT) + 1;
                followeeId = r.nextInt(USERS_COUNT) + 1;

                if (followerId != followeeId) {
                    String key = followerId + "-" + followeeId;
                    if (!usedPairs.contains(key)) {
                        usedPairs.add(key);
                        break;
                    }
                }
            }

            fw.write(generateFollowsInsert(followerId, followeeId));

            if (i != 0 && i % 10000 == 0) {
                fw.write("commit;");
            }
        }

        fw.close();
        System.out.println("\n[Follows] Done!");
    }

    private int generateUserId() {
        return RANDOM.nextInt(USERS_COUNT) + 1;
    }

    private int generatePostId(int min, int max) {
        return RANDOM.nextInt(max - min + 1) + min;
    }
}
