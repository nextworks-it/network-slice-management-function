package it.nextworks.nfvmano.nsmf.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NSST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NST;
import it.nextworks.nfvmano.libs.vs.common.exceptions.NotExistingEntityException;
import it.nextworks.nfvmano.libs.vs.common.nsmf.interfaces.NsiLcmNotificationConsumerInterface;
import it.nextworks.nfvmano.libs.vs.common.nsmf.interfaces.NsmfLcmProvisioningInterface;
import it.nextworks.nfvmano.nsmf.engine.messages.InstantiateNsiRequestMessage;
import it.nextworks.nfvmano.nsmf.engine.messages.NotifyNssiStatusChange;
import it.nextworks.nfvmano.nsmf.record.NsiRecordService;
import it.nextworks.nfvmano.nsmf.engine.messages.NsmfEngineMessage;
import it.nextworks.nfvmano.nsmf.engine.messages.NsmfEngineMessageType;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecord;
import it.nextworks.nfvmano.nsmf.record.elements.NetworkSliceInstanceRecordStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

public class NsLcmManager {

    private static final Logger log = LoggerFactory.getLogger(NsLcmManager.class);
    private UUID networkSliceInstanceId;


    private NST nst;

    private NsiRecordService nsiRecordService;
    private NsmfLcmProvisioningInterface nsmfLcmProvisioningInterface;

    private NsiLcmNotificationConsumerInterface nsiLcmNotificationConsumerInterface;

    public NsLcmManager(UUID networkSliceInstanceId, NST nst, NsiRecordService nsiRecordService,
                        NsmfLcmProvisioningInterface nsmfLcmProvisioningInterface,
                        NsiLcmNotificationConsumerInterface nsiLcmNotificationConsumerInterface) {
        this.networkSliceInstanceId = networkSliceInstanceId;
        this.nst = nst;
        this.nsiRecordService = nsiRecordService;
        this.nsmfLcmProvisioningInterface = nsmfLcmProvisioningInterface;
        this.nsiLcmNotificationConsumerInterface= nsiLcmNotificationConsumerInterface;
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
            }else if(type== NsmfEngineMessageType.MODIFY_NSI_REQUEST){

            }else if(type== NsmfEngineMessageType.NOTIFY_NSSI_STATUS_CHANGE){
                processNotifyNssiStatusChange((NotifyNssiStatusChange)em);
            }else if(type== NsmfEngineMessageType.TERMINATE_NSI_REQUEST){
                processTerminateNsiRequest((InstantiateNsiRequestMessage)em);
            }
        } catch (IOException e) {
           logMessageError(e);
        }

    }

    private void processTerminateNsiRequest(InstantiateNsiRequestMessage em) {
    }

    private void processNotifyNssiStatusChange(NotifyNssiStatusChange em) {
    }

    private void processInstantiateNsiRequest(InstantiateNsiRequestMessage em)  {
        log.debug("Processing Instantiate NSI Request");
        try {
            NetworkSliceInstanceRecord record = nsiRecordService.getNetworkSliceInstanceRecord(em.getRequest().getNsiId());
            if(record.getStatus()==NetworkSliceInstanceRecordStatus.CREATED){

               nsiRecordService.updateNsInstanceStatus(networkSliceInstanceId,
                       NetworkSliceInstanceRecordStatus.COMPUTING_RESOURCE_ALLOCATION,
                       null);

            }else{
                log.warn("Received Instantiate NSI request in wrong status:"+ record.getStatus()+". Ignoring");
            }



        } catch (NotExistingEntityException e) {
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
}
