package it.nextworks.nfvmano.nsmf.record;

import it.nextworks.nfvmano.libs.vs.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceInstance;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecordStatus;
import it.nextworks.nfvmano.nsmf.record.repos.NetworkSliceInstanceRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class NsiRecordService {

    @Autowired
    private NetworkSliceInstanceRepo networkSliceInstanceRepo;

    public UUID createNetworkSliceInstanceEntry(String nstId, String vsInstanceId, String tenantId) {
        NetworkSliceInstanceRecord instanceRecord = new NetworkSliceInstanceRecord(null, nstId, vsInstanceId, NetworkSliceInstanceRecordStatus.CREATED, tenantId);
        networkSliceInstanceRepo.saveAndFlush(instanceRecord);
        return instanceRecord.getId();
    }

    public NetworkSliceInstanceRecord getNetworkSliceInstanceRecord(UUID nsiId) throws NotExistingEntityException {
        Optional<NetworkSliceInstanceRecord> record = networkSliceInstanceRepo.findById(nsiId);
        if(record.isPresent()){
            return record.get();
        }else throw new NotExistingEntityException("Network Slice Instance with ID:"+nsiId.toString()+" NOT found in DB");
    }

    public List<NetworkSliceInstance> getAllNetworkSliceInstance() {
        return networkSliceInstanceRepo.findAll().stream()
                .map(current -> current.getNetworkSliceInstance())
                .collect(Collectors.toList());
    }

    public NetworkSliceInstance getNetworkSliceInstance(String nsiId) throws NotExistingEntityException {
        return this.getNetworkSliceInstanceRecord(UUID.fromString(nsiId)).getNetworkSliceInstance();
    }

    public List<NetworkSliceInstance> getNsInstanceFromNssi(String nssiId) {
        return networkSliceInstanceRepo.findAll().stream()
                .filter(current -> current.getNetworkSliceSubnetInstanceIds().contains(UUID.fromString(nssiId)))
                .map(current -> current.getNetworkSliceInstance())
                .collect(Collectors.toList());

    }

    public void updateNsInstanceStatus(UUID nsiId, NetworkSliceInstanceRecordStatus status, String errorMsg) throws NotExistingEntityException {
        NetworkSliceInstanceRecord record  =    getNetworkSliceInstanceRecord(nsiId);
        record.setStatus(status);
        record.setErrorMsg(errorMsg);
        networkSliceInstanceRepo.saveAndFlush(record);
    }
}
