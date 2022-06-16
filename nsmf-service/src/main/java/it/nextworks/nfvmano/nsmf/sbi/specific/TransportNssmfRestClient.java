package it.nextworks.nfvmano.nsmf.sbi.specific;

import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.exceptions.*;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.configuration.ConfigurationActionType;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.configuration.SatelliteNetworkConfiguration;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.configuration.SliceTransferConfig;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.provisioning.NssmfBaseProvisioningMessage;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.specialized.transport.SdnConfigPayload;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.specialized.transport.TransportInstantiatePayload;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.*;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.record.NsiRecordService;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceSubnetInstanceRecord;
import it.nextworks.nfvmano.nsmf.sbi.NssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.messages.InternalInstantiateNssiRequest;
import it.nextworks.nfvmano.nsmf.sbi.messages.InternalModifyNssiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class TransportNssmfRestClient extends NssmfRestClient {
    private NsiRecordService nsiRecordService;
    private static final Logger log = LoggerFactory.getLogger(TransportNssmfRestClient.class);
    public TransportNssmfRestClient(String url, NsiRecordService nsiRepo) {
        super(url);
        this.nsiRecordService = nsiRepo;
    }

    @Override
    public void instantiateNetworkSubSlice(NssmfBaseProvisioningMessage request) throws MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException, AlreadyExistingEntityException, NotExistingEntityException {
        if(request instanceof InternalInstantiateNssiRequest){
            InternalInstantiateNssiRequest internalRequest = (InternalInstantiateNssiRequest)request;
            ResourceAllocationComputeResponse raResponse = internalRequest.getResourceAllocationComputeResponse();

            Optional<NssResourceAllocation> allocation = raResponse.getNsResourceAllocation().getNssResourceAllocations().stream()
                    .filter(nssA-> nssA.getNsstId().equals(internalRequest.getNsst().getNsstId()))
                    .findFirst();
            if(allocation.isPresent()){
                if(allocation.get().getAllocationType().equals(NssResourceAllocationType.SDN)){
                    TransportInstantiatePayload transportInstantiatePayload=new TransportInstantiatePayload();
                    transportInstantiatePayload.setNssiId(request.getNssiId());
                    transportInstantiatePayload.setNssResourceAllocation(allocation.get());
                    super.instantiateNetworkSubSlice(transportInstantiatePayload);
                }else
                    throw new FailedOperationException("NSS Resource Allocation type not supported");
            } else
                throw new FailedOperationException("Could not find allocation for NSST:"+internalRequest.getNsst().getNsstId());
        } else
            throw  new MethodNotImplementedException("Instantiate network sub slice method not implemented for generic message");
    }


    @Override
    public void terminateNetworkSliceInstance(NssmfBaseProvisioningMessage request) throws NotExistingEntityException, MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException {
        super.terminateNetworkSliceInstance(request);
    }
}
