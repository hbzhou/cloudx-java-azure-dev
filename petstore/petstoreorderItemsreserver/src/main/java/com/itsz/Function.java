package com.itsz;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microsoft.azure.functions.ExecutionContext;
import com.microsoft.azure.functions.HttpMethod;
import com.microsoft.azure.functions.HttpRequestMessage;
import com.microsoft.azure.functions.HttpResponseMessage;
import com.microsoft.azure.functions.HttpStatus;
import com.microsoft.azure.functions.annotation.AuthorizationLevel;
import com.microsoft.azure.functions.annotation.FunctionName;
import com.microsoft.azure.functions.annotation.HttpTrigger;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Optional;

/**
 * Azure Functions with HTTP Trigger.
 */
public class Function {

    private static final String connectionString = "DefaultEndpointsProtocol=https;EndpointSuffix=core.windows.net;AccountName=orderitemreserveraccount;AccountKey=unNf3/WkWx4lFqppMQwVzQLW8ZtKsmFL+DPeui8A8G+zfz0z+fQMtBW/oyLQ226YFd97KVJZKpmz+AStn13cvQ==;BlobEndpoint=https://orderitemreserveraccount.blob.core.windows.net/;FileEndpoint=https://orderitemreserveraccount.file.core.windows.net/;QueueEndpoint=https://orderitemreserveraccount.queue.core.windows.net/;TableEndpoint=https://orderitemreserveraccount.table.core.windows.net/";
    private static final String containerName = "orderitemreserver";

    /**
     * This function listens at endpoint "/api/HttpExample". Two ways to invoke it using "curl" command in bash:
     * 1. curl -d "HTTP Body" {your host}/api/HttpExample
     * 2. curl "{your host}/api/HttpExample?name=HTTP%20Query"
     */
    @FunctionName("updateOrder")
    public HttpResponseMessage run(
            @HttpTrigger(
                name = "req",
                methods = {HttpMethod.GET, HttpMethod.POST},
                authLevel = AuthorizationLevel.ANONYMOUS)
                HttpRequestMessage<Optional<String>> request,
            final ExecutionContext context) throws JsonProcessingException {
        context.getLogger().info("Java HTTP trigger processed a request at" + LocalDateTime.now());

        // Parse query parameter
        final String content = request.getBody().orElse(null);

        if (content == null) {
            return request.createResponseBuilder(HttpStatus.BAD_REQUEST).body("Please pass order string or in the request body").build();
        } else {
            TypeReference<HashMap<String,Object>> typeRef
                    = new TypeReference<HashMap<String,Object>>() {};

            HashMap<String, Object> hashMap = new ObjectMapper().readValue(content, typeRef);
            String sessionId = ((String) hashMap.get("id"));
            BlobServiceClient blobServiceClient = new BlobServiceClientBuilder().connectionString(connectionString).buildClient();
            blobServiceClient.createBlobContainerIfNotExists(containerName).getBlobClient(String.format("%s.json", sessionId))
                    .upload(BinaryData.fromString(content), true);
            return request.createResponseBuilder(HttpStatus.OK).body("Hello, " + content).build();
        }
    }
}
