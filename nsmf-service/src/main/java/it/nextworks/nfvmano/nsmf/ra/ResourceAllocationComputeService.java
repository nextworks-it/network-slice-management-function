package it.nextworks.nfvmano.nsmf.ra;

import it.nextworks.nfvmano.libs.vs.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.vs.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.vs.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.RAAlgorithmType;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.ResourceAllocationPolicy;
import it.nextworks.nfvmano.libs.vs.common.ra.interfaces.ResourceAllocationPolicyMgmt;
import it.nextworks.nfvmano.libs.vs.common.ra.interfaces.ResourceAllocationProvider;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeRequest;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.policy.RAPolicyMatchRequest;
import it.nextworks.nfvmano.nsmf.NsLcmService;
import it.nextworks.nfvmano.nsmf.ra.algorithms.file.FileResourceAllocationAlgorithm;
import it.nextworks.nfvmano.nsmf.ra.algorithms.stat.StaticAlgorithmNXW;
import it.nextworks.nfvmano.nsmf.ra.algorithms.stat.record.StaticRaResponseRepository;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.repos.NetworkSliceInstanceRepo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class ResourceAllocationComputeService implements ResourceAllocationProvider {


    private static final Logger log = LoggerFactory.getLogger(ResourceAllocationComputeService.class);

    @Autowired
    private ResourceAllocationPolicyMgmt resourceAllocationPolicyMgmt;

    @Autowired
    private NetworkSliceInstanceRepo networkSliceInstanceRepo;

    @Autowired
    private NsLcmService nsLcmService;

    @Value("${resource_allocation.default_algorithm:STATIC}")
    private RAAlgorithmType defaultRaAlgorithm;

    @Autowired
    private StaticRaResponseRepository staticRaResponseRepository;

    @Override
    public void computeResources(ResourceAllocationComputeRequest request) throws NotExistingEntityException, FailedOperationException, MalformattedElementException {
        log.debug("Processing request to compute RA");
        NetworkSliceInstanceRecord record = networkSliceInstanceRepo.findById(UUID.fromString(request.getNsiId())).get();
        Optional<ResourceAllocationPolicy> policy = resourceAllocationPolicyMgmt.findMatchingPolicy(new RAPolicyMatchRequest(record.getNstId(), request.getTenant(), null));
        ResourceAllocationProvider algorithm=null;
        if(policy.isPresent()){
            log.debug("Using algorithm:"+policy.get().getAlgorithmType()+" from policy");
            if(policy.get().getAlgorithmType()== RAAlgorithmType.STATIC){
               algorithm = new StaticAlgorithmNXW(this, staticRaResponseRepository);
            } else if(policy.get().getAlgorithmType()== RAAlgorithmType.FILE){
                algorithm= new FileResourceAllocationAlgorithm(this);
            }else throw new FailedOperationException("Unkown algorithm RA type: "+policy.get().getAlgorithmType());
        }else{
            log.debug("No policy found, using default RA algorithm");
            if(RAAlgorithmType.STATIC.equals(defaultRaAlgorithm)){
                algorithm = new StaticAlgorithmNXW(this, staticRaResponseRepository);
            }else if (RAAlgorithmType.FILE.equals(defaultRaAlgorithm)) {
                algorithm = new FileResourceAllocationAlgorithm(this);
            }else throw new FailedOperationException("Unkown algorithm RA type: "+defaultRaAlgorithm);
        }

        algorithm.computeResources(request);

    }

    @Override
    public void processResourceAllocationResponse(ResourceAllocationComputeResponse response) {
        log.debug("Processing Resource Allocation Compute response");
        nsLcmService.processResoureAllocationResponse(response);
    }


}