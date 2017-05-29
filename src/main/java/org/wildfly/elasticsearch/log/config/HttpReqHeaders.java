package org.wildfly.elasticsearch.log.config;

import java.util.LinkedList;
import java.util.List;

/**
 * @author s.samsonov
 */
public class HttpReqHeaders {
    private List<HttpReqHeader> headers = new LinkedList<HttpReqHeader>();

    public List<HttpReqHeader> getHeaders() {
        return headers;
    }

    public void addHeader(HttpReqHeader header) {
        this.headers.add(header);
    }
}
