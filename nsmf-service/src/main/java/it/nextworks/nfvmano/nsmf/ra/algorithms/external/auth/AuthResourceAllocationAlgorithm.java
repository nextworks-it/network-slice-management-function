package it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth;

import it.nextworks.nfvmano.libs.vs.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.vs.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.vs.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.RAAlgorithmType;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeRequest;
import it.nextworks.nfvmano.nsmf.ra.ResourceAllocationComputeService;
import it.nextworks.nfvmano.nsmf.ra.algorithms.BaseResourceAllocationAlgorithm;
import it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.driver.AuthRequestTranslator;
import it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.elements.AuthExternalAlgorithmRequest;
import it.nextworks.nfvmano.nsmf.ra.algorithms.rest.client.ResourceAllocationRestClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AuthResourceAllocationAlgorithm extends BaseResourceAllocationAlgorithm {
    private static final RAAlgorithmType type =RAAlgorithmType.AUTH;
    private static final Logger log = LoggerFactory.getLogger(AuthResourceAllocationAlgorithm.class);
    private String baseUrl;
    private AuthRequestTranslator translator;
    private ResourceAllocationRestClient restClient;

    public AuthResourceAllocationAlgorithm(ResourceAllocationComputeService resourceAllocationComputeService) {
        super(resourceAllocationComputeService, type);
        this.translator=new AuthRequestTranslator();
        this.restClient=new ResourceAllocationRestClient(baseUrl);
    }

    @Override
    public void computeResources(ResourceAllocationComputeRequest resourceAllocationComputeRequest) throws NotExistingEntityException, FailedOperationException, MalformattedElementException {
        log.debug("Received request to compute new resource allocation with AUTH external algorithm");

        AuthExternalAlgorithmRequest authRequest=translator.translateRAComputeRequest(resourceAllocationComputeRequest);

        String responseCode=restClient.computeResourceAllocation(authRequest);
    }
}
