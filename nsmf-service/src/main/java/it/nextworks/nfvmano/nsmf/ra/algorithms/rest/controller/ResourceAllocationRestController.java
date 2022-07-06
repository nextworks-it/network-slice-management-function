package it.nextworks.nfvmano.nsmf.ra.algorithms.rest.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.ra.ResourceAllocationComputeService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping("/resourceallocation")
public class ResourceAllocationRestController {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationRestController.class);

    @Autowired
    private ResourceAllocationComputeService resourceAllocationComputeService;

    public ResourceAllocationRestController(){}

    @RequestMapping(value = "/computedResourceAllocation", method = RequestMethod.POST)
    public void computedResourceAllocation(@RequestBody ResourceAllocationComputeResponse response){
        log.debug("Received response for resource allocation with request ID {}", response.getResponseId());

        ObjectMapper mapper=new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        try {
            log.debug("Resource allocation response \n{}", mapper.writeValueAsString(response));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        resourceAllocationComputeService.processResourceAllocationResponse(response);
    }
}
