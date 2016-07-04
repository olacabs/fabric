package com.olacabs.fabric.processors.httpwriter;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.olacabs.fabric.compute.ProcessingContext;
import com.olacabs.fabric.compute.processor.InitializationException;
import com.olacabs.fabric.compute.processor.ProcessingException;
import com.olacabs.fabric.compute.processor.StreamingProcessor;
import com.olacabs.fabric.compute.util.ComponentPropertyReader;
import com.olacabs.fabric.model.common.ComponentMetadata;
import com.olacabs.fabric.model.event.Event;
import com.olacabs.fabric.model.event.EventSet;
import com.olacabs.fabric.model.processor.Processor;
import com.olacabs.fabric.model.processor.ProcessorType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpEntityEnclosingRequest;
import org.apache.http.HttpRequestInterceptor;
import org.apache.http.HttpResponse;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.AuthState;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.methods.*;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.entity.ByteArrayEntity;
import org.apache.http.impl.auth.BasicScheme;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;


/**
 * Created by avanish.pandey on 26/11/15.
 *
 * A processor that sends events to an http end point .
 */
@EqualsAndHashCode(callSuper = true)
@VisibleForTesting
@Slf4j
@Data
@Processor(
        namespace = "global",
        name = "http-writer",
        version = "1.0.0",
        description = "A processor that sends events to an HTTP end point!!",
        cpu = 0.5,
        memory = 512,
        processorType = ProcessorType.EVENT_DRIVEN,
        requiredProperties = {"endpoint_url", "bulk_supported", "http_method" , "auth_enabled"},
        optionalProperties = {"headers", "should_publish_response", "pool_size", "auth_configuration"}
)
public class HttpWriter extends StreamingProcessor {

    private static final int DEFAULT_POOL_SIZE = 10;
    private int poolSize;
    private ObjectMapper mapper;
    private String headerString;
    private PoolingHttpClientConnectionManager manager;
    private String endPointUrl;
    private Map<String, String> headers;
    private boolean bulkSupported;
    private String httpMethod;
    private CloseableHttpClient client;
    private HttpClientBuilder builder;
    private boolean shouldPublishResponse;
    private Boolean authEnabled;

    /**
     * Starts the HttpClient and Manager
     */
    protected void start(AuthConfiguration authConfiguration , Boolean authEnabled) throws InitializationException {
        this.manager = new PoolingHttpClientConnectionManager();
        manager.setMaxTotal(poolSize);
        manager.setDefaultMaxPerRoute(poolSize);
        //client = HttpClients.custom().setConnectionManager(manager).build();
        builder = HttpClients.custom();
        builder.setConnectionManager(manager);
        if(authEnabled) {
            setAuth(authConfiguration, builder);
        }
        client = builder.build();
    }

    /**
     * Stop the HttpClient and Manager
     *
     * @throws IOException
     */
    private  void stop() throws IOException {
        client.close();
        manager.close();
    }

    @Override
    protected EventSet consume(ProcessingContext processingContext, EventSet eventSet) throws ProcessingException {
        log.debug("Method - {}", this.getHttpMethod().toLowerCase());
        return this.handleRequest(eventSet,this.getHttpMethod().toLowerCase());
    }

    private EventSet handleRequest(EventSet eventSet, String httpMethodType) throws ProcessingException {
        ImmutableList.Builder<Object> builder = ImmutableList.builder();
        EventSet.EventFromEventBuilder eventSetBuilder = EventSet.eventFromEventBuilder();
        HttpRequestBase request = null;
        boolean entityEnclosable = false;
        switch(httpMethodType) {
            case "get":
                request = createHttpGet();
                break;
            case "post":
                request = createHttpPost();
                entityEnclosable = true;
                break;
            case "put":
                request = createHttpPut();
                entityEnclosable = true;
                break;
            default:
                log.error("Method not recognized...{}", this.getHttpMethod());
        }

        request.setHeader("Content-Type", "application/json");
        if (null != this.getHeaders()) {
            this.getHeaders().forEach(request::setHeader);
        }

        CloseableHttpResponse response = null;
        log.debug("Handling Put Request");

        for (Event event : eventSet.getEvents()) {
            try {
                log.debug("Event in json format :  {}", event.getJsonNode());
                if (!isBulkSupported()) {
                    if(entityEnclosable) {
                        ((HttpEntityEnclosingRequest) request).setEntity(new ByteArrayEntity((byte[])event.getData()));
                    }
                    response = getClient().execute(request);
                    handleResponse(response);
                    if (shouldPublishResponse){
                        Event e= Event.builder()
                                .jsonNode(getMapper().convertValue(createResponseMap(event, response, httpMethodType), JsonNode.class))
                                .build();
                        e.setProperties(event.getProperties());
                        eventSetBuilder.isAggregate(false)
                                .partitionId(eventSet.getPartitionId())
                                .event(e);
                    }
                } else {
                    builder.add(event.getData());
                }
            } catch (Exception e) {
                log.error("Error processing data", e);
                throw new ProcessingException();
            } finally {
                close(response);
            }
        }

        if (isBulkSupported()) {
            try {
                log.debug("Making Bulk call as bulk is supported");
                if(entityEnclosable) {
                    ((HttpEntityEnclosingRequest)request).setEntity(new ByteArrayEntity(getMapper().writeValueAsBytes(builder.build())));
                }
                response = client.execute(request);
                handleResponse(response);
            } catch (Exception e) {
                log.error("Exception", e);
                throw new ProcessingException();
            } finally {
                close(response);
            }
        } else {
            if (shouldPublishResponse)
                return eventSetBuilder.build();
        }
        return null;

    }

    private void handleResponse(HttpResponse response) throws IOException, ProcessingException {
        if (null != response) {
            log.debug("Received response {}", response.getStatusLine().getStatusCode());
        } else {
            log.error("No response received from downstream system");
        }
    }

    private HttpPut createHttpPut() {
        HttpPut put = new HttpPut(this.getEndPointUrl());
        return put;
    }



    private HttpGet createHttpGet() {
        HttpGet get = new HttpGet(this.getEndPointUrl());
        return get;
    }


    private void close(CloseableHttpResponse response) {
        if (null != response) {
            try {
                response.close();
            } catch (IOException e) {
                log.error("Error closing http client: ", e);
            }
        }
    }

    private HttpPost createHttpPost() {
        log.debug("Creating http post request");
        HttpPost post = new HttpPost(this.getEndPointUrl());
        return post;
    }

    @Override
    public void initialize(String instanceId, Properties globalProperties, Properties properties, ComponentMetadata componentMetadata) throws InitializationException {
        this.endPointUrl = ComponentPropertyReader.readString(properties, globalProperties, "endpoint_url", instanceId, componentMetadata);
        this.headerString = ComponentPropertyReader.readString(properties, globalProperties, "headers", instanceId, componentMetadata);
        this.httpMethod = ComponentPropertyReader.readString(properties, globalProperties, "http_method", instanceId, componentMetadata);
        this.shouldPublishResponse = ComponentPropertyReader.readBoolean(properties, globalProperties, "should_publish_response", instanceId, componentMetadata, false);
        this.mapper = new ObjectMapper();
        if (!Strings.isNullOrEmpty(headerString)) {
            TypeReference<HashMap<String, String>> typeReference = new TypeReference<HashMap<String, String>>() {
            };
            try {
                this.headers = getMapper().readValue(headerString, typeReference);
            } catch (Exception e) {
                log.error("Error while converting headers", e);
                throw new InitializationException();
            }
        }

        this.bulkSupported = ComponentPropertyReader.readBoolean(properties, globalProperties, "bulk_supported", instanceId, componentMetadata, false);
        this.poolSize = ComponentPropertyReader.readInteger(properties, globalProperties, "pool_size", instanceId, componentMetadata, DEFAULT_POOL_SIZE);
        this.authEnabled = ComponentPropertyReader.readBoolean(properties, globalProperties, "auth_enabled", instanceId, componentMetadata,false);
        try {
            AuthConfiguration authConfiguration = null;
            if(authEnabled) {
                authConfiguration = getMapper().readValue(ComponentPropertyReader.readString(properties, globalProperties, "auth_configuration", instanceId, componentMetadata), AuthConfiguration.class);
            }
            start(authConfiguration , authEnabled);
        } catch (Exception e) {
            log.error("Unable to start the httpclient - {}", e);
            throw new InitializationException();
        }

    }




    /**
     * creates a response map that can be set in the new event set
     *
     * @param event          event to be added in the response
     * @param response       http response
     * @param httpMethodType the http method type
     * @return req
     */
    private Map<String, Object> createResponseMap(Event event, CloseableHttpResponse response, String httpMethodType) throws IOException {
        return ImmutableMap.of("SourceEvent", event.getJsonNode(),
                "HttpMethod", httpMethodType,
                "HttpResponseCode",response.getStatusLine().getStatusCode(),
                "HttpResponse", getResponseAsJson(response));
    }

    private JsonNode getResponseAsJson(CloseableHttpResponse response) throws IOException {
        try {
            if (null != response) {
                final HttpEntity entity = response.getEntity();
                if(null != entity) {
                    final String responseStr = EntityUtils.toString(entity);
                    try {
                        return (!Strings.isNullOrEmpty(responseStr)) ? getMapper().readTree(responseStr) : null;
                    } catch (final Exception e) {
                        return getMapper().createObjectNode().put("Response", responseStr);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Exception while parsing the response - {}", e);
        }
        return getMapper().createObjectNode();
    }

    @Override
    public void destroy() {
        try {
            stop();
        } catch (Exception e) {
            log.error("Error while closing the connection - {}", e);
        }
    }


    private void setAuth(AuthConfiguration authConfiguration, HttpClientBuilder builder ) throws InitializationException{
        if (!Strings.isNullOrEmpty(authConfiguration.getUsername())) {
            Credentials credentials = new UsernamePasswordCredentials(authConfiguration.getUsername(), authConfiguration.getPassword());
            CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
            credentialsProvider.setCredentials(
                    new AuthScope(AuthScope.ANY_HOST, AuthScope.ANY_PORT), credentials);

            builder.addInterceptorFirst((HttpRequestInterceptor) (request, context) -> {
                AuthState authState = (AuthState) context.getAttribute(HttpClientContext.TARGET_AUTH_STATE);
                if (authState.getAuthScheme() == null) {
                    //log.debug("SETTING CREDS");
                    //log.info("Preemptive AuthState: {}", authState);
                    authState.update(new BasicScheme(), credentials);
                }
            });

        } else {
            log.error("Username can't be blank for basic auth.");
            throw new InitializationException("Username blank for basic auth");
        }
    }


}

