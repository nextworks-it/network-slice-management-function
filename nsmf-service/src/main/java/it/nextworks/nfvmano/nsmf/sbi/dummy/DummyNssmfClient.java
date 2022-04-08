package it.nextworks.nfvmano.nsmf.sbi.dummy;

import it.nextworks.nfvmano.libs.vs.common.exceptions.*;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.provisioning.NssmfBaseProvisioningMessage;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.ComputeNssResourceAllocation;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.NssResourceAllocation;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.sbi.NssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.messages.InternalInstantiateNssiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DummyNssmfClient extends NssmfRestClient {
    private final static Logger log= LoggerFactory.getLogger(DummyNssmfClient.class);

    public DummyNssmfClient(String url) {
        super(url);
    }

    @Override
    public UUID createNetworkSubSliceIdentifier() throws MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException, IllegalAccessException, ClassNotFoundException, InstantiationException {
        return UUID.randomUUID();
    }

    @Override
    public void instantiateNetworkSubSlice(NssmfBaseProvisioningMessage request) throws MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException, AlreadyExistingEntityException, NotExistingEntityException {
        if(request instanceof InternalInstantiateNssiRequest) {
            InternalInstantiateNssiRequest internalRequest = (InternalInstantiateNssiRequest) request;
            ResourceAllocationComputeResponse raResponse = internalRequest.getResourceAllocationComputeResponse();
            Optional<NssResourceAllocation> allocation = raResponse.getNsResourceAllocation().getNssResourceAllocations().stream()
                    .filter(nssA -> nssA.getNsstId().equals(internalRequest.getNsst().getNsstId()))
                    .findFirst();
            if (allocation.isPresent()) {
                log.debug("Received request to instantiate nssi with ID {}", request.getNssiId());
                log.debug("Creation of NS with ID {}", internalRequest.getNsst().getNsdInfo().getNsdId());
                Map<String, String> vnfPlacement=((ComputeNssResourceAllocation) allocation.get()).getVnfPlacement();

                for(String vnfdId: vnfPlacement.keySet()){
                    log.debug("VNF with ID {} placed on node {}", vnfdId, vnfPlacement.get(vnfdId));
                }
            } else
                throw new FailedOperationException("Could not find allocation for NSST:" + internalRequest.getNsst().getNsstId());
        } else
            throw  new MethodNotImplementedException("Instantiate network sub slice method not implemented for generic message");
    }
}
