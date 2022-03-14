package it.nextworks.nfvmano.nsmf.ra.algorithms.external.AUTH;

import it.nextworks.nfvmano.libs.vs.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.vs.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.vs.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.RAAlgorithmType;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeRequest;
import it.nextworks.nfvmano.nsmf.ra.ResourceAllocationComputeService;
import it.nextworks.nfvmano.nsmf.ra.algorithms.BaseResourceAllocationAlgorithm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthResourceAllocationAlgorithm extends BaseResourceAllocationAlgorithm {
    private static final RAAlgorithmType type =RAAlgorithmType.AUTH;
    private static final Logger log = LoggerFactory.getLogger(AuthResourceAllocationAlgorithm.class);

    public AuthResourceAllocationAlgorithm(ResourceAllocationComputeService resourceAllocationComputeService) {
        super(resourceAllocationComputeService, type);
    }
    @Override
    public void computeResources(ResourceAllocationComputeRequest resourceAllocationComputeRequest) throws NotExistingEntityException, FailedOperationException, MalformattedElementException {
        log.debug("Received request to compute new resource allocation with AUTH external algorithm");


    }
}
