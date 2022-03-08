package it.nextworks.nfvmano.nsmf.ra.algorithms.rest.client;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.ExternalAlgorithmRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.*;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

public class ResourceAllocationRestClient {
    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationRestClient.class);

    private String externalAlgorithmBaseUrl;
    private RestTemplate restTemplate;
    private String cookies;

    public ResourceAllocationRestClient(){}

    public ResourceAllocationRestClient(String externalAlgorithmBaseUrl){
        this.externalAlgorithmBaseUrl=externalAlgorithmBaseUrl;
        this.restTemplate=new RestTemplate();

    }

    private ResponseEntity<String> performHTTPRequest(Object request, String url, HttpMethod httpMethod/*, String tenantID*/) {
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        //performAuth(tenantID);
        if (this.cookies != null) {
            header.add("Cookie", this.cookies);
        }

        ObjectMapper mapper=new ObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        String policyString="";
        try {
            policyString = mapper.writerWithDefaultPrettyPrinter().writeValueAsString(request);
        } catch (JsonProcessingException e){
            log.error("Cannot serialize policy");
            return null;
        }
        log.debug(policyString);
        HttpEntity<?> httpEntity = new HttpEntity<>(policyString, header);

        try {
            //log.info("URL performing the request to: "+url);
            ResponseEntity<String> httpResponse =
                    restTemplate.exchange(url, httpMethod, httpEntity, String.class);
            HttpStatus code = httpResponse.getStatusCode();
            //log.info("Response code: " + httpResponse.getStatusCode().toString());
            return httpResponse;
        } catch (RestClientException e) {
            log.info("Message received: " + e.getMessage());
            return null;
        }
    }

    private String manageHTTPResponse(ResponseEntity<?> httpResponse, String errMsg, String okCodeMsg, HttpStatus httpStatusExpected) {
        if (httpResponse == null) {
            log.info(errMsg);
            return null;
        }

        if (httpResponse.getStatusCode().equals(httpStatusExpected)) log.info(okCodeMsg);
        else log.info(errMsg);

        log.info("Response code: " + httpResponse.getStatusCode().toString());

        if (httpResponse.getBody() == null) return null;

        log.info(("Body response: " + httpResponse.getBody().toString()));
        return httpResponse.getBody().toString();
    }

    public String computeResourceAllocation(ExternalAlgorithmRequest request){
        log.debug("Sending request to compute resource allocation to external algorithm");
        String url=externalAlgorithmBaseUrl+"/computeResourceAllocation";
        ResponseEntity<String> httpResponse= performHTTPRequest(request, url, HttpMethod.PUT);
        return manageHTTPResponse(httpResponse, "Error while computing resource allocation", "Resource allocation request arrived", HttpStatus.ACCEPTED);
    }
}
