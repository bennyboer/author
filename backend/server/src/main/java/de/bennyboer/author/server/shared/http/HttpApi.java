package de.bennyboer.author.server.shared.http;

import java.util.HashMap;
import java.util.Map;

public class HttpApi {

    private final Map<String, String> aggregateTypeToUrl = new HashMap<>();

    public HttpApi registerHttpApiUrl(String aggregateType, String url) {
        aggregateTypeToUrl.put(aggregateType, url);
        return this;
    }

    public String getUrlByAggregateType(String host, String aggregateType) {
        return aggregateTypeToUrl.get(aggregateType).replace("{HOST}", host);
    }

}
