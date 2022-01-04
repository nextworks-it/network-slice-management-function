package it.nextworks.nfvmano.nsmf.sbi;

import it.nextworks.nfvmano.libs.vs.common.exceptions.*;
import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceInstance;
import it.nextworks.nfvmano.libs.vs.common.nsmf.interfaces.NsmfLcmConfigInterface;
import it.nextworks.nfvmano.libs.vs.common.nsmf.interfaces.NsmfLcmProvisioningInterface;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.provisioning.CreateNsiIdRequest;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.provisioning.InstantiateNsiRequest;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.provisioning.TerminateNsiRequest;
import it.nextworks.nfvmano.libs.vs.common.query.messages.GeneralizedQueryRequest;

import java.util.List;
import java.util.UUID;

public class NssmfRestClient implements NsmfLcmProvisioningInterface, NsmfLcmConfigInterface {
    @Override
    public UUID createNetworkSliceIdentifier(CreateNsiIdRequest request, String tenantId) throws NotExistingEntityException, MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException {
        return null;
    }

    @Override
    public void instantiateNetworkSlice(InstantiateNsiRequest request, String tenantId) throws NotExistingEntityException, MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException {

    }

    @Override
    public void terminateNetworkSliceInstance(TerminateNsiRequest request, String tenantId) throws NotExistingEntityException, MethodNotImplementedException, FailedOperationException, MalformattedElementException, NotPermittedOperationException {

    }

    @Override
    public List<NetworkSliceInstance> queryNetworkSliceInstance(GeneralizedQueryRequest request, String tenantId) throws MalformattedElementException {
        return null;
    }
}
