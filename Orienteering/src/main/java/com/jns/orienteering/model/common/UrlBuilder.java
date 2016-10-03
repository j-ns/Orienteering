package com.jns.orienteering.model.common;

import com.jns.orienteering.util.Validators;

public class UrlBuilder {

    protected static final String JSON_SUFFIX = ".json";

    private String                baseUrl;

    public UrlBuilder() {
    }

    public UrlBuilder(String baseUrl) {
        if (baseUrl.startsWith("/")) {
            this.baseUrl = "/" + baseUrl;
        } else {
            this.baseUrl = baseUrl;
        }
    }

    public String buildUrl(String... urlParts) {
        return buildPath(urlParts) + JSON_SUFFIX;
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

    /**
     * Builds a path from <code>urlParts</code>
     *
     * @param urlParts
     * @return path with leading backslash or empty String if null
     */
    public String buildPath(String... urlParts) {
        String result = "";

        for (String child : urlParts) {
            if (Validators.isNullOrEmpty(child)) { // test:
                throw new IllegalArgumentException("urlPart cant be empty");
            }

            if (!child.startsWith("/")) {
                result = result + "/" + child;
            } else {
                result = result + child;
            }
        }
        return result;
    }

}