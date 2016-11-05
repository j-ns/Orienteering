package com.jns.orienteering.model.repo;

import static com.jns.orienteering.util.Validators.isNullOrEmpty;

public class UrlBuilder {

    private static final String JSON_SUFFIX = ".json";

    private final String        baseUrl;

    public UrlBuilder(String baseUrl) {
        if (!baseUrl.startsWith("/")) {
            this.baseUrl = "/" + baseUrl;
        } else {
            this.baseUrl = baseUrl;
        }
    }

    public String buildUrlFromRelativePath(String... urlParts) {
        if (baseUrl == null) {
            throw new IllegalStateException("baseUrl can not be null");
        }
        if (urlParts.length > 0) {
            return baseUrl + buildPath(urlParts) + JSON_SUFFIX;
        }
        return baseUrl + JSON_SUFFIX;
    }

    public static String buildUrl(String... urlParts) {
        return buildPath(urlParts) + JSON_SUFFIX;
    }

    /**
     * Builds a path from <code>urlParts</code>
     *
     * @param urlParts
     * @return path with leading backslash or empty String if null
     */
    public static String buildPath(String... urlParts) {
        String result = "";

        for (String child : urlParts) {
            if (isNullOrEmpty(child)) {
                continue;
            }

            if (!child.startsWith("/")) {
                result = result + "/" + child;
            } else {
                result = result + child;
            }
        }
        if (isNullOrEmpty(result)) {
            throw new IllegalArgumentException("url can not be empty");
        }
        return result;
    }

}