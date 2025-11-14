package org.staging.tool;

import com.github.javafaker.Faker;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

public class Tool {
    private static final Random random = new Random();
    private static final Faker faker = new Faker(new Locale("en"), random);

    private static HashSet<String> usernames = new HashSet<>();

    public static String generateUserInsert() {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        String firstPart = firstName.length() >= 2
                ? firstName.substring(0, 2)
                : firstName;

        String username = "";
        String baseUsername = "";
        while (!usernames.contains(username)) {
            baseUsername = (firstPart + lastName)
                    .toLowerCase()
                    .replaceAll("[^a-z]", "");

            String suffix = faker.letterify("???") + faker.numerify("#####");
            username = baseUsername + suffix;
            usernames.add(username);
        }

        String fullName = firstName + " " + lastName;
        String domain = faker.internet().domainName();
        String email = username + "@" + domain;
        String bio = switch (random.nextInt(12)) {
            case 0 -> faker.chuckNorris().fact();
            case 1 -> faker.superhero().descriptor();
            case 2 -> faker.yoda().quote();
            case 3 -> faker.howIMetYourMother().quote();
            case 4 -> faker.harryPotter().quote();
            case 5 -> faker.funnyName().name();
            case 6 -> faker.elderScrolls().quote();
            case 7 -> faker.gameOfThrones().quote();
            case 8 -> faker.lebowski().quote();
            case 9 -> faker.witcher().quote();
            case 10 -> faker.rickAndMorty().quote();
            default -> faker.leagueOfLegends().quote();
        };

        return String.format("""
                        INSERT INTO users ( username, full_name, email, bio ) VALUES ( '%s', '%s', '%s', '%s' );
                        """,
                escape(escapeWeirdChars(username)),
                escape(escapeWeirdChars(fullName)),
                escape(escapeWeirdChars(email)),
                escape(escapeWeirdChars(bio)));
    }

    public static String generatePostInsert(int authorId, int replyTo) {
        String body = faker.lorem().paragraph();
        if (replyTo == 0) {
            return String.format("""
                    INSERT INTO posts ( author_id, body ) VALUES ( %d, '%s' );
                    """, authorId, escape(body));
        } else {
            return String.format("""
                    INSERT INTO posts ( author_id, body, reply_to_id ) VALUES ( %d, %d, '%s', %d );
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

        switch (random.nextInt(15)) {
            case 0 -> tag.append(faker.chuckNorris().fact().split(" ")[0]);
            case 1 -> tag.append(faker.superhero().name().replaceAll("\\s+", ""));
            case 2 -> tag.append(faker.animal().name().replaceAll("\\s+", ""));
            case 3 -> tag.append(faker.beer().name().replaceAll("\\s+", ""));
            case 4 -> tag.append(faker.space().planet().replaceAll("\\s+", ""));
            case 5 -> tag.append(faker.music().genre().replaceAll("\\s+", ""));
            case 6 -> tag.append(faker.ancient().hero().replaceAll("\\s+", ""));
            case 7 -> tag.append(faker.ancient().god().replaceAll("\\s+", ""));
            case 9 -> tag.append(faker.ancient().primordial().replaceAll("\\s+", ""));
            case 10 -> tag.append(faker.book().genre().replaceAll("\\s+", ""));
            case 11 -> tag.append(faker.backToTheFuture().character().replaceAll("\\s+", ""));
            case 12 -> tag.append(faker.color().name().replaceAll("\\s+", ""));
            case 13 -> tag.append(faker.aquaTeenHungerForce().character().replaceAll("\\s+", ""));
            default -> tag.append(faker.food().dish().replaceAll("\\s+", ""));
        }

        if (random.nextDouble() < 0.8) {
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

    private static String escapeWeirdChars(String input) {
        if (input == null) return "";

        String escaped = input
                .replaceAll("[\\r\\n\\t]+", " ");

        escaped = escaped.replaceAll("[\"'\\\\/]", "");

        escaped = escaped.trim().replaceAll("\\s{2,}", " ");

        return escaped;
    }
}
