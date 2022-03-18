package it.nextworks.nfvmano.nsmf.ra.algorithms.rest.client;

import it.nextworks.nfvmano.libs.vs.common.ra.elements.ExternalAlgorithmRequest;
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

    private ResponseEntity<String> performHTTPRequest(Object request, String url, HttpMethod httpMethod) {
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");
        if (this.cookies != null) {
            header.add("Cookie", this.cookies);
        }

        HttpEntity<?> httpEntity = new HttpEntity<>(request, header);

        try {
            ResponseEntity<String> httpResponse =
                    restTemplate.exchange(url, httpMethod, httpEntity, String.class);
            HttpStatus code = httpResponse.getStatusCode();
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
        return manageHTTPResponse(httpResponse, "Error while computing resource allocation", "Resource allocation computation started", HttpStatus.ACCEPTED);
    }
}
