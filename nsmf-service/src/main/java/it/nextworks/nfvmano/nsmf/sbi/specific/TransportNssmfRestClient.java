package it.nextworks.nfvmano.nsmf.sbi.specific;

import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.exceptions.*;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.provisioning.NssmfBaseProvisioningMessage;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.specialized.transport.SdnConfigPayload;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.NssResourceAllocation;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.VirtualLinkResourceAllocation;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.record.NsiRecordService;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceSubnetInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.repos.NetworkSliceInstanceRepo;
import it.nextworks.nfvmano.nsmf.sbi.NssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.messages.InternalInstantiateNssiRequest;
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
            SdnConfigPayload sdnConfigPayload = new SdnConfigPayload();
            sdnConfigPayload.setNssiId(request.getNssiId());
            NetworkSliceInstanceRecord nsiRecord = nsiRecordService.getNetworkSliceInstanceRecord(internalRequest.getParentNsiId());
            log.debug("Retrieving CORE slice");
            for(NetworkSliceSubnetInstanceRecord nssiRecord : nsiRecord.getNetworkSliceSubnetInstanceIds()){
                log.debug("NSSI ID:"+nssiRecord.getNssiIdentifier()+" type:"+nssiRecord.getSliceSubnetType());
                if(nssiRecord.getSliceSubnetType().equals(SliceSubnetType.CORE)){
                    sdnConfigPayload.setTargetNssiId(nssiRecord.getNssiIdentifier());
                }
            }

            Optional<NssResourceAllocation> allocation = raResponse.getNsResourceAllocation().getNssResourceAllocations().stream()
                    .filter(nssA-> nssA.getNsstId().equals(internalRequest.getNsst().getNsstId()))
                    .findFirst();
            if(allocation.isPresent()){

                List<Map<String, String>> transportSpecifications = new ArrayList<>();
                for(VirtualLinkResourceAllocation vlAllocation: allocation.get().getvLinkResources()){
                    Map<String, String> curTAlloc = new HashMap<>();
                    curTAlloc.put("transport-id", vlAllocation.getVirtualLinkId());
                    if(vlAllocation.isDefault())
                        sdnConfigPayload.setTargetTransportId(vlAllocation.getVirtualLinkId());
                    if(vlAllocation.getDefaultGw()!=null)
                        curTAlloc.put("gateway-id", vlAllocation.getDefaultGw());
                    transportSpecifications.add(curTAlloc);
                }

                sdnConfigPayload.setTransportSpecifications(transportSpecifications);
                super.instantiateNetworkSubSlice(sdnConfigPayload);
            }else throw new FailedOperationException("Could not find allocation for NSST:"+internalRequest.getNsst().getNsstId());

        }else throw  new MethodNotImplementedException("Instantiate network sub slice method not implemented for generic message");

    }
}
