package org.staging.tool;

import com.github.javafaker.Faker;
import org.staging.tool.data.JsonMeta;
import org.staging.tool.data.UserSettings;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.Locale;
import java.util.Random;

public class Tool {
    private static final Random random = new Random();
    private static final Faker faker = new Faker(new Locale("en"), random);

    private static HashSet<String> usernames = new HashSet<>();

    public static String generateUserInsert(boolean photoAllowed) throws IOException {
        String firstName = faker.name().firstName();
        String lastName = faker.name().lastName();

        String fileHex = "EMPTY_BLOB()";
        if (photoAllowed) {
            fileHex = "HEXTORAW('" + fileToHex(new File("./photo/vtak2.jpg")) + "')";
        }

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


        String address = faker.address().streetAddress();
        String birthdaySql = "DATE '" + faker.date().birthday(18, 90).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString() + "'";
        int isPrivate = random.nextBoolean() ? 1 : 0;
        String profileObj =
                "t_profile_obj('" +
                        escape(address) + "', " +
                        birthdaySql + ", " +
                        isPrivate +
                        ")";
        String settings = generateUserSettings().toString();

        return String.format(
                "INSERT INTO x_users (username, full_name, email, bio, profile_picture, profile_obj, settings) " +
                        "VALUES ('%s','%s','%s','%s', %s, %s, '%s');\n",
                escape(escapeWeirdChars(username)),
                escape(escapeWeirdChars(fullName)),
                escape(escapeWeirdChars(email)),
                escape(escapeWeirdChars(bio)),
                fileHex,
                profileObj,
                settings
        );

    }

    public static String generatePostInsert(int authorId, int replyTo) {
        String body = faker.lorem().paragraph();

        LocalDate baseDate = faker.date().birthday(0, 5).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate();

        String createdAt = "DATE '" + baseDate.toString() + "'";
        String updatedAt = createdAt;

        if (random.nextDouble() < 0.5) {
            updatedAt = "DATE '" + baseDate.plusDays(random.nextInt(360)).toString() + "'";
        }

        String json = generate().toString();

        if (replyTo == 0) {
            return String.format("""
                    INSERT INTO x_posts ( author_id, body, json_meta, created_at, updated_at ) VALUES ( %d, '%s', '%s', %s, %s );
                    """, authorId, escape(body), json, createdAt, updatedAt);
        } else {
            return String.format("""
                    INSERT INTO x_posts ( author_id, body, reply_to_id, json_meta, created_at, updated_at ) VALUES ( %d, '%s', %d, '%s', %s, %s );
                    """, authorId, escape(body), replyTo, json, createdAt, updatedAt);
        }
    }

    public static String generateMediaInsert(int postId, boolean binAllowed) throws IOException {
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

        String fileHex = "EMPTY_BLOB()";
        if (binAllowed) {
            fileHex = "HEXTORAW('" + fileToHex(new File("./photo/1kb.wav")) + "')";
        }

        String date = "DATE '" + faker.date().birthday(0, 5).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString() + "'";

        return String.format("""
                        INSERT INTO x_media ( post_id, mime_type, bin_data, caption, created_at ) VALUES ( %d, '%s', %s, '%s', %s );
                        """,
                postId,
                escape(mimeType),
                fileHex,
                escape(caption),
                date);
    }

    public static String generateReactionInsert(int userId, int postId) {
        String[] types = {"like", "love", "smile", "cry", "share"};
        String type = types[random.nextInt(types.length)];

        String date = "DATE '" + faker.date().birthday(0, 5).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString() + "'";

        return String.format("""
                        INSERT INTO x_reactions ( user_id, post_id, type, created_at ) VALUES ( %d, %d, '%s', %s );
                        """,
                userId,
                postId,
                type,
                date);
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
                INSERT INTO x_tags ( name ) VALUES ( '%s' );
                """, randomHashtag());
    }

    public static String generatePostTagsInsert(int postId, int tagId) {
        return String.format("""
                INSERT INTO x_post_tags ( post_id, tag_id ) VALUES ( %d, %d );
                """, postId, tagId);
    }

    public static String generateFollowsInsert(int followerId, int followeeId) {
        String date = "DATE '" + faker.date().birthday(0, 5).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString() + "'";

        return String.format("""
                INSERT INTO x_follows ( follower_id, followee_id, created_at ) VALUES ( %d, %d, %s );
                """, followerId, followeeId, date);
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

    public static String fileToHex(File file) throws IOException {
        FileInputStream fis = new FileInputStream(file);
        byte[] bytes = fis.readAllBytes();
        fis.close();

        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }
        return sb.toString();
    }

    public static UserSettings generateUserSettings() {
        UserSettings s = new UserSettings();

        String[] visibility = {"public", "private", "followers"};
        String[] messages = {"everyone", "followers", "none"};
        String[] themes = {"light", "dark"};
        String[] languages = {"en", "sk", "de", "cz"};

        s.setProfileVisibility(pick(visibility));
        s.setMessagesFrom(pick(messages));
        s.setTheme(pick(themes));
        s.setLanguage(pick(languages));

        s.setShowOnline(random.nextBoolean());
        s.setNotifyLikes(random.nextBoolean());
        s.setNotifyComments(random.nextBoolean());
        s.setNotifyFollows(random.nextBoolean());
        s.setTwoFactor(random.nextBoolean());
        s.setSensitiveContent(random.nextBoolean());

        return s;
    }

    private static String pick(String[] arr) {
        return arr[random.nextInt(arr.length)];
    }

    public static JsonMeta generate() {
        JsonMeta meta = new JsonMeta();

        String[] devices = {"desktop", "mobile", "tablet"};
        String[] countries = {"SK", "CZ", "AT", "DE", "PL"};
        String[] userAgents = {
                "Mozilla/5.0 (Windows NT 10.0)",
                "Mozilla/5.0 (Macintosh; Intel Mac OS X)",
                "Mozilla/5.0 (Linux; Android 11)",
                "Mozilla/5.0 (iPhone; CPU iPhone OS 15_0)",
                "Mozilla/5.0 (iPad; CPU OS 14_5)"
        };

        meta.device = pick(devices);
        meta.ipAddress = randomIp();
        meta.country = pick(countries);
        meta.userAgent = pick(userAgents);
        meta.timestamp = '"' + faker.date().birthday(0, 5).toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDate()
                .toString() + '"';

        return meta;
    }

    private static String randomIp() {
        return random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256) + "." +
                random.nextInt(256);
    }
}
