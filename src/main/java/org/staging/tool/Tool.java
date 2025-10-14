package org.staging.tool;

import com.github.javafaker.Faker;

import java.util.Locale;
import java.util.Random;

public class Tool {
    private static final Random random = new Random();
    private static final Faker faker = new Faker(new Locale("en"), random);

    public static String generateUserInsert() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        String baseUsername = (firstName.charAt(0) + lastName).toLowerCase().replaceAll("[^a-z]", "");
        if (random.nextBoolean()) {
            baseUsername += random.nextInt(100);
        }

        String fullName = firstName + " " + lastName;
        String domain = faker.internet().domainName();
        String email = baseUsername + "@" + domain;
        String bio = faker.yoda().quote();

        return String.format("""
                        INSERT INTO users ( username, full_name, email, bio ) VALUES ( '%s', '%s', '%s', '%s' );
                        """,
                escape(baseUsername),
                escape(fullName),
                escape(email),
                escape(bio));
    }

    public static String generatePostInsert(int authorId, int replyTo) {
        String body = faker.lorem().paragraph();
        if (replyTo == 0) {
            return String.format("""
                    INSERT INTO posts ( author_id, body ) VALUES ( %d, '%s' );
                    """, authorId, escape(body));
        } else {
            return String.format("""
                    INSERT INTO posts ( author_id, body, reply_to_id ) VALUES ( %d, '%s', %d );
                    """, authorId, escape(body), replyTo);
        }
    }

    public static String generateMediaInsert(int postId) {
        String[] mimeTypes = {
                "image/jpeg",
                "image/png",
                "image/webp",
                "video/mp4",
                "video/webm"
        };
        String mimeType = mimeTypes[random.nextInt(mimeTypes.length)];

        String caption = faker.lorem().sentence(8).trim();
        if (caption.length() > 239) {
            caption = caption.substring(0, 239);
        }

        return String.format("""
                        INSERT INTO media ( post_id, mime_type, caption ) VALUES ( %d, '%s', '%s' );
                        """,
                postId,
                escape(mimeType),
                escape(caption));
    }

    public static String generateReactionInsert(int userId, int postId) {
        String[] types = {"like", "love", "smile", "cry", "share"};
        String type = types[random.nextInt(types.length)];

        return String.format("""
                        INSERT INTO reactions ( user_id, post_id, type ) VALUES ( %d, %d, '%s' );
                        """,
                userId,
                postId,
                type);
    }

    private static String randomHashtag() {
        StringBuilder tag = new StringBuilder("#");

        switch (random.nextInt(8)) { // pick random theme
            case 0 -> tag.append(faker.chuckNorris().fact().split(" ")[0]);
            case 1 -> tag.append(faker.superhero().name().replaceAll("\\s+", ""));
            case 2 -> tag.append(faker.animal().name().replaceAll("\\s+", ""));
            case 3 -> tag.append(faker.beer().name().replaceAll("\\s+", ""));
            case 4 -> tag.append(faker.programmingLanguage().name().replaceAll("\\s+", ""));
            case 5 -> tag.append(faker.space().planet().replaceAll("\\s+", ""));
            case 6 -> tag.append(faker.music().genre().replaceAll("\\s+", ""));
            default -> tag.append(faker.food().dish().replaceAll("\\s+", ""));
        }

        // randomly add a “mood” or “energy” suffix
        if (random.nextDouble() < 0.3) {
            String mood = faker.lorem().word().replaceAll("[^a-zA-Z]", "");
            tag.append(mood.substring(0, 1).toUpperCase()).append(mood.substring(1).toLowerCase());
        }

        return tag.toString().toLowerCase();
    }


    public static String generateTagInsert() {
        return String.format("""
                INSERT INTO tags ( name ) VALUES ( '%s' );
                """, randomHashtag());
    }

    public static String generatePostTagsInsert(int postId, int tagId) {
        return String.format("""
                INSERT INTO post_tags ( post_id, tag_id ) VALUES ( %d, %d );
                """, postId, tagId);
    }

    public static String generateFollowsInsert(int followerId, int followeeId) {
        return String.format("""
                INSERT INTO follows ( follower_id, followee_id ) VALUES ( %d, %d );
                """, followerId, followeeId);
    }

    private static String escape(String text) {
        return text.replace("'", "''");
    }
}
