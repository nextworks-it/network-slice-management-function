package it.nextworks.nfvmano.nsmf.ra;

import it.nextworks.nfvmano.libs.vs.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.vs.common.ra.interfaces.ResourceAllocationPolicyMgmt;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.policy.OnboardRAPolicyRequest;
import it.nextworks.nfvmano.nsmf.NsLcmService;
import it.nextworks.nfvmano.nsmf.ra.record.elements.ResourceAllocationPolicyRecord;
import it.nextworks.nfvmano.nsmf.ra.record.repos.ResourceAllocationPolicyRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ResourceAllocationPolicyService implements ResourceAllocationPolicyMgmt {

    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationPolicyService.class);
    @Autowired
    private ResourceAllocationPolicyRepo resourceAllocationPolicyRepo;

    @Override
    public void onboardResourceAllocationPolicy(OnboardRAPolicyRequest request) throws MalformattedElementException {
        log.debug("Processing request to Onboard a new RA policy");
        request.isValid();
        ResourceAllocationPolicyRecord record = new ResourceAllocationPolicyRecord(request.getResourceAllocationPolicy());
        resourceAllocationPolicyRepo.saveAndFlush(record);
        log.debug("RA policy onboard:"+record.getId());
    }

    @Override
    public void deleteResourceAllocationPolicy(UUID raPolicyId) {

    }
}
