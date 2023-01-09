package it.nextworks.nfvmano.nsmf.ra.algorithms.file;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.libs.vs.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.NsResourceAllocation;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.RAAlgorithmType;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeRequest;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.ra.ResourceAllocationComputeService;
import it.nextworks.nfvmano.nsmf.ra.algorithms.BaseResourceAllocationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class FileResourceAllocationAlgorithm extends BaseResourceAllocationAlgorithm {
    private static final RAAlgorithmType type =RAAlgorithmType.FILE ;

    private String defaultRaFilePath;

    private static final Logger log = LoggerFactory.getLogger(FileResourceAllocationAlgorithm.class);
    public FileResourceAllocationAlgorithm(ResourceAllocationComputeService resourceAllocationComputeService, String defaultRaFilePath) {
        super(resourceAllocationComputeService, type);
        this.defaultRaFilePath=defaultRaFilePath;
    }

    @Override
    public void computeResources(ResourceAllocationComputeRequest request) throws FailedOperationException {
        log.debug("Received ResourceAllocationCompute request");

        ObjectMapper mapper = new ObjectMapper();
        ResourceAllocationComputeResponse defaultResponse;

        String raFilename = defaultRaFilePath + "ra-" + request.getNst().getNstId() + ".json";
        try {
            defaultResponse = mapper.readValue(new File(raFilename), ResourceAllocationComputeResponse.class);
        } catch (IOException e) {
            log.error("Error reading " + raFilename + " Resource Allocation response file.");
            throw new FailedOperationException("Could not read " + raFilename +
                    " Resource Allocation response file. Aborting.");
        }

        NsResourceAllocation nsResourceAllocation =
                new NsResourceAllocation(UUID.randomUUID().toString(),request.getNsiId(),
                        defaultResponse.getNsResourceAllocation().getNssResourceAllocations() );
        ResourceAllocationComputeResponse response =
                new ResourceAllocationComputeResponse(request.getRequestId(), nsResourceAllocation, true);

        try {
            log.debug("Selected Resource Allocation {}", mapper.writeValueAsString(defaultResponse));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(2000);

                    processResourceAllocationResponse(response);
                } catch (InterruptedException e) {
                    log.error("Error",e);
                }
            }
        });
        thread.start();
    }
}
