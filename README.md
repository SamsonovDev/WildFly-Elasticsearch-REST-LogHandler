# Wildfly-Elasticsearch-REST-LogHandler
Elasticsearch log handler sends logs to elasticsearch cluster using REST API.<br><br>
<b>Configuration</b><br>
1.Copy modules/org/wildfly to $WILDFLY_HOME/modules/system/layers/base/org. 
<br>
2.Edit WildFly configuration file<br> 
1) adding custom-handler for ElasticsearchCustomHandler:<br>
<pre><code>&lt;custom-handler name="ELASTICSEARCH" class="org.wildfly.elasticsearch.log.ElasticsearchCustomHandler" module="org.wildfly.elasticsearch.log"&gt;
  &lt;formatter&gt;
    &lt;pattern-formatter pattern="%d{HH:mm:ss,SSS} %-5p [%c] (%t) %s%E%n"/&gt;
  &lt;/formatter&gt;
  &lt;properties&gt;
    &lt;property name="index" value="logs-%tY-%tm-%td"/&gt;
    &lt;property name="indexType" value="server"/&gt;
    &lt;property name="url" value="<URL to send WildFly logs to Elasticsearch>"/&gt;
    &lt;property name="checkRetryInterval" value="true"/&gt;
    &lt;property name="retryInterval" value="30000"/&gt;
    &lt;property name="connectTimeout" value="30000"/&gt;
    &lt;property name="readTimeout" value="30000"/&gt;
    &lt;property name="maxQueueSize" value="10485760"/&gt;
    &lt;property name="enabled" value="true"/&gt;
  &lt;/properties&gt;
&lt;/custom-handler&gt;</code></pre>
2) adding the ELASTICSEARCH handler to root-logger:<br>
<pre><code>&lt;root-logger&gt;
  &lt;level name="DEBUG"/&gt;
  &lt;handlers&gt;
    &lt;handler name="CONSOLE"/&gt;
    &lt;handler name="ELASTICSEARCH"/&gt;
  &lt;/handlers>&gt;
&lt;/root-logger&gt;</code></pre>
<b>Properties</b><br>
index - Elasticsearch index to send WildFly logs;<br>
indexType - URL Elasticsearch bulk API endpoint;<br>
url - URL of Elasticsearch bulk API endpoint;<br>
checkRetryInterval - if it is set to true and Elasticsearch host is available then the Elasticsearch handler repeat attempt to connect to the Elasticsearch host in the interval specified in the property retryInterval. If 
checkRetryInterval is set to false then the handler checks the Elasticsearch host when sending WildFly logs.<br>
retryInterval - the interval between attempts of connecting to the Elasticsearch host (ms);<br>
maxRetries - maximum of attempts to connect to the Elasticsearch host;<br>
connectionTimeout -  timeout of connection to the Elasticsearch host (ms);<br>
The interval between attempts of connecting to the Elasticsearch host is set in the property retryInterval (ms). Maximum of attempts to connect to the Elasticsearch host is set in the property maxRetries. Timeout of connection to the Elasticsearch host is set in the property connectionTimeout (ms).<br>
readTimeout - Elasticsearch read timeout (ms);<br>
maxQueueSize - maximum size of log message buffer;<br>
enabled - if it set to true then Wildfly logs are processed in the handler, otherwise sending Wildfly logs to Elasticsearch is disabled.

