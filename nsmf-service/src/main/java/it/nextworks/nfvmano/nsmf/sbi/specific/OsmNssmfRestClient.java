package it.nextworks.nfvmano.nsmf.sbi.specific;

import it.nextworks.nfvmano.libs.ifa.templates.nst.NSST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.exceptions.*;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.provisioning.NssmfBaseProvisioningMessage;
import it.nextworks.nfvmano.libs.vs.common.nssmf.messages.specialized.ran.OsmPayload;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.sbi.NssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.messages.InternalInstantiateNssiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OsmNssmfRestClient extends NssmfRestClient {
    private final static Logger log= LoggerFactory.getLogger(OsmNssmfRestClient.class);

    public OsmNssmfRestClient(String url) {
        super(url);
    }


    private NSST getNsst(NST nst, SliceSubnetType sliceSubnetType){
        for(NSST nsst: nst.getNsst().getNsstList()){
            if(nsst.getType() == sliceSubnetType){
                return nsst;
            }
        }
        return null;
    }
    @Override
    public void instantiateNetworkSubSlice(NssmfBaseProvisioningMessage request) throws MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException, AlreadyExistingEntityException, NotExistingEntityException {
        if (request instanceof InternalInstantiateNssiRequest) {
            InternalInstantiateNssiRequest internalRequest = (InternalInstantiateNssiRequest) request;
            ResourceAllocationComputeResponse raResponse = internalRequest.getResourceAllocationComputeResponse();
            raResponse.getNsResourceAllocation().getNssResourceAllocations().get(0).getAllocationType();
            NSST nsstRan = getNsst(internalRequest.getParentNst(), SliceSubnetType.RAN); //TODO FIXX
            OsmPayload osmPayload = new OsmPayload();
            if (nsstRan!=null)
            {
            log.info("Requesting instantation of NSSI with UUID "+internalRequest.getNssiId().toString());
            osmPayload.setNssiId(internalRequest.getNssiId());
            osmPayload.setNsdId(nsstRan.getNsdInfo().getNsdId());
    }
            else{
                throw new MalformattedElementException("Check OSM payload. must contain the nsdID and nsst id");
            }
            super.instantiateNetworkSubSlice(osmPayload);

        }

    }
}
