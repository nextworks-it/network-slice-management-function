package it.nextworks.nfvmano.nsmf.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NSST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.exceptions.*;
import it.nextworks.nfvmano.libs.vs.common.nsmf.interfaces.NsiLcmNotificationConsumerInterface;
import it.nextworks.nfvmano.libs.vs.common.nsmf.interfaces.NsmfLcmProvisioningInterface;
import it.nextworks.nfvmano.libs.vs.common.nssmf.interfaces.NssmfLcmProvisioningInterface;
import it.nextworks.nfvmano.libs.vs.common.ra.interfaces.ResourceAllocationProvider;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeRequest;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.engine.messages.*;
import it.nextworks.nfvmano.nsmf.record.NsiRecordService;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecordStatus;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceSubnetRecordStatus;
import it.nextworks.nfvmano.nsmf.sbi.NssmfDriverRegistry;
import it.nextworks.nfvmano.nsmf.sbi.generic.GenericInstantiateNssiRequest;
import it.nextworks.nfvmano.nsmf.sbi.messages.InternalInstantiateNssiRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;
import java.util.stream.Collectors;

public class NsLcmManager {

    private static final Logger log = LoggerFactory.getLogger(NsLcmManager.class);
    private UUID networkSliceInstanceId;


    private NST nst;

    private NsiRecordService nsiRecordService;
    private NsmfLcmProvisioningInterface nsmfLcmProvisioningInterface;

    private NsiLcmNotificationConsumerInterface nsiLcmNotificationConsumerInterface;
    private ResourceAllocationProvider resourceAllocationProvider;
    private NssmfDriverRegistry driverRegistry;
    private ResourceAllocationComputeResponse resourceAllocationComputeResponse;
    private ArrayList<NSST> nsstToInstantiate = new ArrayList<>();
    public NsLcmManager(UUID networkSliceInstanceId, NST nst, NsiRecordService nsiRecordService,
                        NsmfLcmProvisioningInterface nsmfLcmProvisioningInterface,
                        NsiLcmNotificationConsumerInterface nsiLcmNotificationConsumerInterface,
                        ResourceAllocationProvider resourceAllocationProvider,
                        NssmfDriverRegistry driverRegistry) {
        this.networkSliceInstanceId = networkSliceInstanceId;
        this.nst = nst;
        this.nsiRecordService = nsiRecordService;
        this.nsmfLcmProvisioningInterface = nsmfLcmProvisioningInterface;
        this.nsiLcmNotificationConsumerInterface= nsiLcmNotificationConsumerInterface;
        this.resourceAllocationProvider=resourceAllocationProvider;
        this.driverRegistry= driverRegistry;
    }



    public void receiveMessage(String message) {
        log.debug("Received message for NSI " + networkSliceInstanceId + "\n" + message);

        ObjectMapper mapper = new ObjectMapper();
        NsmfEngineMessage em = null;
        try {
            em = (NsmfEngineMessage) mapper.readValue(message, NsmfEngineMessage.class);

            NsmfEngineMessageType type = em.getType();
            log.debug("Processing internal "+ type+ " message");
            if(type == NsmfEngineMessageType.INSTANTIATE_NSI_REQUEST){

                processInstantiateNsiRequest((InstantiateNsiRequestMessage)em);
            }else if(type== NsmfEngineMessageType.NOTIFY_RESOURCE_ALLOCATION_RESPONSE){
                processResourceAllocationNotification((NotifyResourceAllocationResponse)em);

            }else if(type== NsmfEngineMessageType.MODIFY_NSI_REQUEST){

            }else if(type== NsmfEngineMessageType.NOTIFY_NSSI_STATUS_CHANGE){
                processNotifyNssiStatusChange((EngineNotifyNssiStatusChange)em);
            }else if(type== NsmfEngineMessageType.TERMINATE_NSI_REQUEST){
                processTerminateNsiRequest((InstantiateNsiRequestMessage)em);
            }
        } catch (IOException e) {
           logMessageError(e);
        }

    }


    private void internalInstantiateFsmUpdate(){
        log.debug("Updating instance FSM");
        try {
            NetworkSliceInstanceRecord record = nsiRecordService.getNetworkSliceInstanceRecord(this.networkSliceInstanceId);
            if(record.getStatus().equals(NetworkSliceInstanceRecordStatus.COMPUTING_RESOURCE_ALLOCATION)  ||
                    record.getStatus().equals(NetworkSliceInstanceRecordStatus.INSTANTIATING_CORE_SUBNET) ||
                    record.getStatus().equals(NetworkSliceInstanceRecordStatus.INSTANTIATING_TRANSPORT_SUBNET) ||
                    record.getStatus().equals(NetworkSliceInstanceRecordStatus.INSTANTIATING_RAN_SUBNET) ||
                    record.getStatus().equals(NetworkSliceInstanceRecordStatus.INSTANTIATING_APP_SUBNET))
            {

                if(nsstToInstantiate.isEmpty()){
                    log.debug("Completed NSST instantiation. Setting NSI to INSTANTIATED");
                    nsiRecordService.updateNsInstanceStatus(this.networkSliceInstanceId, NetworkSliceInstanceRecordStatus.INSTANTIATED, "");
                }else{

                    NSST targetNsst = nsstToInstantiate.get(0);
                    log.debug("Instantiating NSST with id:"+targetNsst.getNsstId());
                    NetworkSliceInstanceRecordStatus status=null;
                    if(targetNsst.getType().equals(SliceSubnetType.CORE)){
                        status = NetworkSliceInstanceRecordStatus.INSTANTIATING_CORE_SUBNET;

                    }else if(targetNsst.getType().equals(SliceSubnetType.TRANSPORT)){
                        status = NetworkSliceInstanceRecordStatus.INSTANTIATING_TRANSPORT_SUBNET;
                    }else if(targetNsst.getType().equals(SliceSubnetType.RAN)){
                        status = NetworkSliceInstanceRecordStatus.INSTANTIATING_RAN_SUBNET;
                    }else if(targetNsst.getType().equals(SliceSubnetType.VAPP)){
                        status = NetworkSliceInstanceRecordStatus.INSTANTIATING_APP_SUBNET;
                    }
                    log.debug("Updating NSI status to: "+status);
                    nsiRecordService.updateNsInstanceStatus(this.networkSliceInstanceId, status, "");
                    NssmfLcmProvisioningInterface driver = getNssmfLcmDriver(this.resourceAllocationComputeResponse, targetNsst);

                    try {
                        UUID nssiId = driver.createNetworkSubSliceIdentifier();
                        log.debug("created NSSI ID:"+nssiId);
                        nsiRecordService.createNetworkSliceSubnetInstanceEntry(targetNsst.getNsstId(), nssiId, this.networkSliceInstanceId, targetNsst.getType());
                        log.debug("created NSSI RECORD:"+nssiId+" nsstId:"+targetNsst.getNsstId()+" type:"+targetNsst.getType());
                        driver.instantiateNetworkSubSlice(new InternalInstantiateNssiRequest(nssiId,
                                this.networkSliceInstanceId,
                                targetNsst,
                                resourceAllocationComputeResponse, this.nst ));
                    } catch (MethodNotImplementedException e) {
                        failInstance(e.getMessage());
                    } catch (FailedOperationException e) {
                        failInstance(e.getMessage());
                    } catch (MalformattedElementException e) {
                        failInstance(e.getMessage());
                    } catch (NotPermittedOperationException e) {
                        failInstance(e.getMessage());
                    } catch (IllegalAccessException e) {
                        failInstance(e.getMessage());
                    } catch (ClassNotFoundException e) {
                        failInstance(e.getMessage());
                    } catch (InstantiationException e) {
                        failInstance(e.getMessage());
                    } catch (AlreadyExistingEntityException e) {
                        failInstance(e.getMessage());
                    }
                }

            }



        } catch (NotExistingEntityException e) {
            failInstance(e.getMessage());
        }

    }

    private void processResourceAllocationNotification(NotifyResourceAllocationResponse em) {
        log.debug("Processing resource allocation notification");
        NetworkSliceInstanceRecord record = null;
        try {
            record = nsiRecordService.getNetworkSliceInstanceRecord(this.networkSliceInstanceId);
            if(!record.getStatus().equals(NetworkSliceInstanceRecordStatus.COMPUTING_RESOURCE_ALLOCATION)){
                log.warn("Received Resource Allocation notification in wrong status. IGNORING");
                return;
            }

            if(!em.getResponse().isSuccessful()) {
                failInstance("Could not find Resource Allocation Solution");
                return;
            }
            this.resourceAllocationComputeResponse= em.getResponse();


            Optional<NSST> coreNsst = nst.getNsst().getNsstList().stream().filter(nsst -> nsst.getType().equals(SliceSubnetType.CORE)).findFirst();
            if(coreNsst.isPresent()){
                log.debug("Found CORE NSST: "+coreNsst.get().getNsstId());
                nsstToInstantiate.add(coreNsst.get());

            }
            Optional<NSST> transportNsst = nst.getNsst().getNsstList().stream().filter(nsst -> nsst.getType().equals(SliceSubnetType.TRANSPORT)).findFirst();
            if(transportNsst.isPresent()) {
                  log.debug("Found TRANSPORT NSST: " + transportNsst.get().getNsstId());
                  nsstToInstantiate.add(transportNsst.get());
            }

            Optional<NSST> ranNsst = nst.getNsst().getNsstList().stream().filter(nsst -> nsst.getType().equals(SliceSubnetType.RAN)).findFirst();
            if(ranNsst.isPresent()) {
                log.debug("Found RAN NSST: " + ranNsst.get().getNsstId());
                nsstToInstantiate.add(ranNsst.get());
            }

            Optional<NSST> appNsst = nst.getNsst().getNsstList().stream().filter(nsst -> nsst.getType().equals(SliceSubnetType.VAPP)).findFirst();
            if(appNsst.isPresent()) {
                log.debug("Found VAPP NSST: " + appNsst.get().getNsstId());
                nsstToInstantiate.add(appNsst.get());
            }

            if(nsstToInstantiate.isEmpty()){
                failInstance("No CORE/TRANSPORT/EDGE/RAN/APP NSST to instantiate");
                return;
            }

            log.debug("Computed NSST instantiation order:"+nsstToInstantiate.stream().map(nsst -> nsst.getNsstId()).collect(Collectors.toList()));
            internalInstantiateFsmUpdate();

        } catch (NotExistingEntityException e) {
            failInstance(e.getMessage());
        }

    }

    private void processTerminateNsiRequest(InstantiateNsiRequestMessage em) {
    }

    private void processNotifyNssiStatusChange(EngineNotifyNssiStatusChange em) {
        log.debug("Processing NSSI Status Change notification for NSSI:"+em.getNssiId());
        try {
            NetworkSliceInstanceRecord record = nsiRecordService.getNetworkSliceInstanceRecord(this.networkSliceInstanceId);
            if(record.getStatus()==NetworkSliceInstanceRecordStatus.INSTANTIATING_CORE_SUBNET
             || record.getStatus()==NetworkSliceInstanceRecordStatus.INSTANTIATING_TRANSPORT_SUBNET
            || record.getStatus()==NetworkSliceInstanceRecordStatus.INSTANTIATING_RAN_SUBNET
                    || record.getStatus()==NetworkSliceInstanceRecordStatus.INSTANTIATING_APP_SUBNET){
                if(em.isSuccessful()){
                    //TODO: validate incoming notification
                    log.debug("NSSI sucessfully instantiated");
                    log.debug("Removing NSST from pending instantiation list:"+nsstToInstantiate.get(0).getNsstId());
                    nsstToInstantiate.remove(0);
                    nsiRecordService.updateNetworkSliceSubnetStatus(em.getNssiId(), NetworkSliceSubnetRecordStatus.INSTANTIATED);
                    internalInstantiateFsmUpdate();
                }else{
                    failInstance("Failed NSSI status change:"+em.getNssiId());
                }
            }else log.warn("Received NOTIFY NSSI STATUS Change in wrong status: "+record.getStatus()+". IGNORING");

        } catch (NotExistingEntityException e) {
            failInstance("Failed to retrieve NS Instance Record from DB:"+this.networkSliceInstanceId);
        }
    }

    private void processInstantiateNsiRequest(InstantiateNsiRequestMessage em)  {
        log.debug("Processing Instantiate NSI Request");
        try {
            NetworkSliceInstanceRecord record = nsiRecordService.getNetworkSliceInstanceRecord(em.getRequest().getNsiId());
            if(record.getStatus()==NetworkSliceInstanceRecordStatus.CREATED){

               nsiRecordService.updateNsInstanceStatus(networkSliceInstanceId,
                       NetworkSliceInstanceRecordStatus.COMPUTING_RESOURCE_ALLOCATION,
                       null);
                resourceAllocationProvider.computeResources(new ResourceAllocationComputeRequest(this.networkSliceInstanceId.toString(), em.getTenantId()));
            }else{
                log.warn("Received Instantiate NSI request in wrong status:"+ record.getStatus()+". Ignoring");
            }



        } catch (NotExistingEntityException | FailedOperationException | MalformattedElementException e) {
           failInstance(e.getMessage());
        }

    }

    private void failInstance(String message){
        try {
            log.error("Error during LCM operation:"+ message);
            nsiRecordService.updateNsInstanceStatus(networkSliceInstanceId, NetworkSliceInstanceRecordStatus.FAILED, message);
        } catch (NotExistingEntityException e) {
            log.error("Error while retrieving instance record from DB");
        }
    }

    private void logMessageError(Exception e){
        log.error("Exception during message exchange. Skipping message");
        log.error("Error message:", e);

    }


    private NssmfLcmProvisioningInterface getNssmfLcmDriver(ResourceAllocationComputeResponse response, NSST targetNsst){
        return driverRegistry.getNssmfLcmDriver(response, targetNsst);
    }
}
