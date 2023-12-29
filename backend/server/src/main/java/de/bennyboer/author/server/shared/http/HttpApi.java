package de.bennyboer.author.server.shared.http;

import java.util.HashMap;
import java.util.Map;

public class HttpApi {

    private final Map<String, String> aggregateTypeToUrl = new HashMap<>();

    public void registerHttpApiUrl(String aggregateType, String url) {
        aggregateTypeToUrl.put(aggregateType, url);
    }

    public String getUrlByAggregateType(String aggregateType) {
        return aggregateTypeToUrl.get(aggregateType);
    }

}
