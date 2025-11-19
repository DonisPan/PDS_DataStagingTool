package org.staging.tool.data;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class UserSettings {
    private String profileVisibility;
    private String messagesFrom;
    private String theme;
    private String language;
    private boolean showOnline;
    private boolean notifyLikes;
    private boolean notifyComments;
    private boolean notifyFollows;
    private boolean twoFactor;
    private boolean sensitiveContent;

    @Override
    public String toString() {
        return "{\"profileVisibility\":\"" + profileVisibility + "\"," +
                "\"messagesFrom\":\"" + messagesFrom + "\"," +
                "\"theme\":\"" + theme + "\"," +
                "\"language\":\"" + language + "\"," +
                "\"showOnline\":" + showOnline + "," +
                "\"notifyLikes\":" + notifyLikes + "," +
                "\"notifyComments\":" + notifyComments + "," +
                "\"notifyFollows\":" + notifyFollows + "," +
                "\"twoFactor\":" + twoFactor + "," +
                "\"sensitiveContent\":" + sensitiveContent + "}";
    }

}