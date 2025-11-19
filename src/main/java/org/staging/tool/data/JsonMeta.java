package org.staging.tool.data;

public class JsonMeta {

    public String device;
    public String ipAddress;
    public String country;
    public String userAgent;
    public String timestamp;

    @Override
    public String toString() {
        return "{" +
                "\"device\":\"" + device + "\"," +
                "\"ipAddress\":\"" + ipAddress + "\"," +
                "\"country\":\"" + country + "\"," +
                "\"userAgent\":\"" + userAgent + "\"," +
                "\"timestamp\":" + timestamp +
                "}";
    }

}