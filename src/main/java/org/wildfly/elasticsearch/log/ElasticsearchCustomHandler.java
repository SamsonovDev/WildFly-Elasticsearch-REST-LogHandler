package org.wildfly.elasticsearch.log;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import org.wildfly.elasticsearch.log.config.HttpReqHeaders;
import org.wildfly.elasticsearch.log.writer.ElasticsearchLogWriter;
import org.wildfly.elasticsearch.log.config.Settings;

import javax.xml.bind.DatatypeConverter;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Calendar;
import java.util.logging.Handler;
import java.util.logging.LogRecord;


/**
 * Elasticsearch custom log handler sends logs to Elasticsearch using REST API.
 * @author s.samsonov
 */
public class ElasticsearchCustomHandler extends Handler {

    private boolean enabled;
    private String indexName = "wildfly";
    private String indexTemplate;
    private String indexType;
    private Calendar currentCalendar;
    private ElasticsearchLogWriter logWriter;
    private JsonFactory jsonFactory;
    private JsonGenerator jsonGenerator;
    private Settings settings;
    private URL url;

    protected HttpReqHeaders headers;

    private final Object lock;

    public ElasticsearchCustomHandler() throws MalformedURLException, IOException {
        super();
        this.lock = new Object();
        try {
            this.settings = new Settings();
        } catch (Exception e) {
            e.getMessage();
        }
    }

    void processLogRecord(final LogRecord record) {
        try {
            synchronized (lock) {
                if (enabled) {
                    if (this.jsonGenerator == null) {
                        initLogWriter(this.headers, this.settings);
                    }
                    if (record != null) {
                        serializeEvents(jsonGenerator, record);
                    }
                    logWriter.sendData();
                }
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    void initLogWriter(HttpReqHeaders headers, Settings settings) {
        try {
            this.headers = new HttpReqHeaders();
            this.logWriter = new ElasticsearchLogWriter(headers, settings);
            this.jsonFactory = new JsonFactory();
            this.jsonFactory.setRootValueSeparator(null);
            this.jsonGenerator = jsonFactory.createGenerator(logWriter);
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    private void serializeEvents(JsonGenerator gen, LogRecord record) throws IOException {
        serializeIndexInfo(gen, record);
        gen.writeRaw('\n');
        serializeLogRecord(gen, record);
        gen.writeRaw('\n');
        gen.flush();
    }

    private void serializeIndexInfo(JsonGenerator gen, LogRecord record) throws IOException {
        gen.writeStartObject();
        gen.writeObjectFieldStart("index");
        gen.writeObjectField(
                "_index",
                ((record == null)
                        ?this.indexName
                        :getIndexName(this.indexTemplate, record.getMillis()))
        );
        gen.writeObjectField("_type", indexType);
        gen.writeEndObject();
        gen.writeEndObject();
    }

    private void serializeLogRecord(JsonGenerator gen, LogRecord record) throws IOException {
        gen.writeStartObject();
        gen.writeObjectField("@timestamp", getTimestamp(record.getMillis()));
        gen.writeObjectField("severity", record.getLevel().getName());
        gen.writeObjectField("message", getFormattedMessage(record));
        gen.writeObjectField("threadId", String.valueOf(record.getThreadID()));
        gen.writeObjectField("sequenceId", String.valueOf(record.getSequenceNumber()));
        gen.writeObjectField("logger", record.getLoggerName());
        gen.writeObjectField("sourceClassName", record.getSourceClassName());
        gen.writeObjectField("sourceMethodName", record.getSourceMethodName());
        gen.writeEndObject();
    }

    @Override
    public void publish(final LogRecord record) {
        processLogRecord(record);
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(final boolean enabled) {
        this.enabled = enabled;
    }

    public String getIndex() {
        return indexName;
    }

    public void setIndex(final String index) {
        String indexString = index.replace("%t","%1$t");
        if (indexString.equalsIgnoreCase(index)) {
            this.indexName = index;
        } else {
            this.indexTemplate = indexString;
        }
    }

    public String getIndexType() {
        return indexType;
    }

    public void setIndexType(final String indexType) {
        this.indexType = indexType;
    }

    public URL getURL() {
        return this.settings.getUrl();
    }

    public void setUrl(final String url) {
        try {
            this.settings.setUrl(new URL(url));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    public boolean getCheckRetryInterval() {
        return this.settings.getCheckRetryInterval();
    }

    public void setCheckRetryInterval(final String checkRetryInterval) {
        try {
            this.settings.setCheckRetryInterval(new Boolean(checkRetryInterval));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getRetryInterval() {
        return this.settings.getRetryInterval();
    }

    public void setRetryInterval(final String retryInterval) {
        try {
            this.settings.setRetryInterval(new Long(retryInterval));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getConnectTimeout() {
        return this.settings.getConnectTimeout();
    }

    public void setConnectTimeout(final String connectTimeout) {
        try {
            this.settings.setConnectTimeout(Integer.parseInt(connectTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getReadTimeout() {
        return this.settings.getReadTimeout();
    }

    public void setReadTimeout(final String readTimeout) {
        try {
            this.settings.setReadTimeout(Integer.parseInt(readTimeout));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getMaxQueueSize() {
        return this.settings.getMaxQueueSize();
    }

    public void setMaxQueueSize(final String maxQueueSize) {
        try {
            this.settings.setMaxQueueSize(Integer.parseInt(maxQueueSize));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public long getMaxRetries() {
        return this.settings.getMaxRetries();
    }

    public void setMaxRetries(final String maxRetries) {
        try {
            this.settings.setMaxRetries(Integer.parseInt(maxRetries));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected String getIndexName(String idxTemplate, long timestamp) {
        if (!((idxTemplate == null)||("".equals(idxTemplate)))) {
            Calendar logCalendar = processCalendarInstance(timestamp);
            if ((this.currentCalendar == null)
                    || (logCalendar.get(Calendar.DAY_OF_YEAR) != this.currentCalendar.get(Calendar.DAY_OF_YEAR))
                    || (logCalendar.get(Calendar.YEAR) != this.currentCalendar.get(Calendar.YEAR))) {
                this.currentCalendar = logCalendar;
                this.indexName = String.format(idxTemplate, logCalendar);
            }
        }
        return this.indexName;
    }

    protected static String getTimestamp(long timestamp) {
        return DatatypeConverter.printDateTime(
                processCalendarInstance(timestamp));
    }

    protected static Calendar processCalendarInstance(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(timestamp);
        return cal;
    }

    protected static String getFormattedMessage(LogRecord logRecord) {
        String messageStr = "";
        if ((logRecord.getParameters() == null) || (logRecord.getParameters().length == 0)) {
            messageStr = logRecord.getMessage();
        } else {
            messageStr = String.format(logRecord.getMessage(),logRecord.getParameters());
        }
        return messageStr;
    }
}
