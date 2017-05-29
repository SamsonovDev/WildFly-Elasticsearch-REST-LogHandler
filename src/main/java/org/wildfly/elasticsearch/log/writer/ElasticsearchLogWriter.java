package org.wildfly.elasticsearch.log.writer;

import org.wildfly.elasticsearch.log.config.HttpReqHeader;
import org.wildfly.elasticsearch.log.config.HttpReqHeaders;
import org.wildfly.elasticsearch.log.config.Settings;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;

/**
 * @author s.samsonov
 */
public class ElasticsearchLogWriter extends Writer {
    private boolean logBufferExceeded;

    private boolean checkRetryInterval = true;

    private HttpReqHeaders headers;

    private Collection<HttpReqHeader> headersCollection;

    private int connectTimeout;

    private int readTimeout;

    private int maxQueueSize;

    private int maxRetries;

    private long retryInterval;

    private long startTimer;

    private StringBuilder logBuffer;

    private URL url;

    public ElasticsearchLogWriter(HttpReqHeaders httpReqHeaders, Settings settings) throws MalformedURLException {
        this.url = settings.getUrl();
        this.checkRetryInterval = settings.getCheckRetryInterval();
        this.connectTimeout = settings.getConnectTimeout();
        this.readTimeout = settings.getReadTimeout();
        this.maxQueueSize = settings.getMaxQueueSize();
        this.maxRetries = settings.getMaxRetries();
        this.retryInterval = settings.getRetryInterval();
        this.logBuffer = new StringBuilder();
        this.headers = httpReqHeaders;
        this.headersCollection = headers != null && headers.getHeaders() != null
                ? headers.getHeaders()
                : Collections.<HttpReqHeader>emptyList();
    }

    public void write(char[] cbuf, int off, int len) {
        if (logBufferExceeded) {
            return;
        }

        logBuffer.append(cbuf, off, len);

        if (logBuffer.length() >= maxQueueSize) {
            logBufferExceeded = true;
        }
    }

    public void sendData() throws IOException {
        if (logBuffer.length() <= 0) {
            return;
        }
        try {
            URL elasticUrl = url;
            InetAddress ipAddress = InetAddress.getByName(elasticUrl.getHost());

            if (((!checkRetryInterval)
                    || (checkRetryInterval && ((System.currentTimeMillis() > (retryInterval + startTimer)) || (startTimer == 0L))))
                    && (ipAddress.isReachable(null, maxRetries, connectTimeout))) {
                HttpURLConnection elasticConnection = (HttpURLConnection) (elasticUrl.openConnection());
                try {
                    elasticConnection.setDoInput(true);
                    elasticConnection.setDoOutput(true);
                    elasticConnection.setReadTimeout(readTimeout);
                    elasticConnection.setConnectTimeout(connectTimeout);
                    elasticConnection.setRequestMethod("POST");

                    String message = logBuffer.toString();

                    if (!headersCollection.isEmpty()) {
                        for (HttpReqHeader header : headersCollection) {
                            elasticConnection.setRequestProperty(header.getName(), header.getValue());
                        }
                    }

                    Writer writer = new OutputStreamWriter(elasticConnection.getOutputStream(), "UTF-8");
                    writer.write(message);
                    writer.flush();
                    writer.close();

                    int responseCode = elasticConnection.getResponseCode();

                    if (responseCode != 200) {
                        String errData = processErrors(elasticConnection);
                        throw new IOException("Received response code ["
                                + responseCode + "] from Elasticsearch host with data " + errData);
                    }
                } finally {
                    elasticConnection.disconnect();

                    if (startTimer > 0L) {
                        startTimer = 0L;
                    }
                }
            } else {
                if (startTimer == 0L) {
                    startTimer = System.currentTimeMillis();
                }
            }
        } finally {
            logBuffer.setLength(0);

            if (logBufferExceeded) {
                logBufferExceeded = false;
            }
        }
    }

    private static String processErrors(HttpURLConnection elasticConnection) {
        try {
            InputStream errStream = elasticConnection.getErrorStream();
            if (errStream == null) {
                return "<no data found>";
            }

            StringBuilder errBuilder = new StringBuilder();

            InputStreamReader errReader = new InputStreamReader(errStream, "UTF-8");
            char[] errBuf = new char[2048];
            int errCharNum;

            while ((errCharNum = errReader.read(errBuf)) > 0) {
                errBuilder.append(errBuf, 0, errCharNum);
            }
            return errBuilder.toString();
        } catch (Exception e) {
            return "<error retrieving data: " + e.getMessage() + ">";
        }
    }

    @Override
    public void flush() throws IOException {}

    @Override
    public void close() throws IOException {}

}
