package com.jns.orienteering.model.repo;

import java.util.Arrays;
import java.util.List;

import com.gluonhq.connect.provider.RestClient;

class RestClientFactory {

    private static final String   APP_ID          = "https://orienteering-2dd97.firebaseio.com";
    private static final String   AUTH_PARAM_NAME = "auth";
    private static final String   CREDENTIALS     = "2ekET9SyGxrYCeSWgPZaWdiCHxncCHmAvGCjDjwu";

    protected static final String GET             = "GET";
    protected static final String PUT             = "PUT";
    protected static final String POST            = "POST";

    private RestClientFactory() {
    }

    static RestClient baseClient() {
        RestClient client = RestClient.create().host(APP_ID);
        client.queryParam(AUTH_PARAM_NAME, CREDENTIALS);
        // client.queryParam("print", "pretty");
        return client;
    }

    static RestClient queryClient(String url) {
        return create(GET, url, QueryParameter.shallow());
    }

    static RestClient queryClient(List<QueryParameter> queryParams, String url) {
        return create(GET, url, queryParams);
    }

    static RestClient deleteClient(String url) {
        return create(POST, url, QueryParameter.deleteOverride(), QueryParameter.shallow());
    }

    static RestClient create(String method, String url, QueryParameter queryParameter) {
        return create(method, url, Arrays.asList(queryParameter));
    }

    static RestClient create(String method, String url, QueryParameter... queryParameters) {
        return create(method, url, Arrays.asList(queryParameters));
    }

    static RestClient create(String method, String url, List<QueryParameter> queryParameters) {
        RestClient client = baseClient();
        client.method(method);
        client.path(url);
        for (QueryParameter param : queryParameters) {
            client.queryParam(param.getKey(), param.getValue());
        }
        return client;
    }
}
