package it.nextworks.nfvmano.nsmf.record;

import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceInstance;
import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceSubnetInstance;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecordStatus;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceSubnetInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceSubnetRecordStatus;
import it.nextworks.nfvmano.nsmf.record.repos.NetworkSliceInstanceRepo;
import it.nextworks.nfvmano.nsmf.record.repos.NetworkSliceSubnetInstanceRepo;
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

    @Autowired
    private NetworkSliceSubnetInstanceRepo networkSliceSubnetInstanceRepo;

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

    public NetworkSliceSubnetInstanceRecord getNetworkSliceSubnetInstanceRecord(UUID nssiId) throws NotExistingEntityException {
        Optional<NetworkSliceSubnetInstanceRecord> record = networkSliceSubnetInstanceRepo.findByNssiIdentifier(nssiId);
        if(record.isPresent()){
            return record.get();
        }else throw new NotExistingEntityException("Network Slice Subnet Instance with ID:"+nssiId.toString()+" NOT found in DB");
    }

    public List<NetworkSliceInstance> getAllNetworkSliceInstance() {
        return networkSliceInstanceRepo.findAll().stream()
                .map(current -> current.getNetworkSliceInstance())
                .collect(Collectors.toList());
    }

    public List<NetworkSliceSubnetInstance> getAllNetworkSliceSubnetInstance() {
        return networkSliceSubnetInstanceRepo.findAll().stream()
                .map(current -> current.getNetworkSliceSubnetInstance())
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

    public void createNetworkSliceSubnetInstanceEntry(String nsstId, UUID nssiIdentifier, UUID parentNsiId, SliceSubnetType sliceSubnetType) throws NotExistingEntityException {
        NetworkSliceSubnetInstanceRecord instanceRecord = new NetworkSliceSubnetInstanceRecord( nsstId, nssiIdentifier, parentNsiId,  NetworkSliceSubnetRecordStatus.INSTANTIATING,sliceSubnetType);

        networkSliceSubnetInstanceRepo.saveAndFlush(instanceRecord);
        Optional<NetworkSliceInstanceRecord> nsiRecord = networkSliceInstanceRepo.findById(parentNsiId);
        if(nsiRecord.isPresent()){
            nsiRecord.get().addNetworkSliceSubnetInstance(instanceRecord);
            networkSliceInstanceRepo.saveAndFlush(nsiRecord.get());
        }else throw new NotExistingEntityException("Could not find parent NSI with id: "+parentNsiId);

    }

    public void updateNetworkSliceSubnetStatus(UUID nssiId, NetworkSliceSubnetRecordStatus status )throws NotExistingEntityException{
        NetworkSliceSubnetInstanceRecord nssiRecord = getNetworkSliceSubnetInstanceRecord(nssiId);
        nssiRecord.setStatus(status);
        networkSliceSubnetInstanceRepo.save(nssiRecord);
    }



}
