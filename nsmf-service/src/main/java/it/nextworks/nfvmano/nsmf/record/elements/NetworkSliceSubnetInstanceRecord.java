package it.nextworks.nfvmano.nsmf.record.elements;

import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceInstance;
import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceSubnetInstance;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class NetworkSliceSubnetInstanceRecord {
    @Id
    private UUID nssiIdentifier;

    private NetworkSliceSubnetRecordStatus status;

    private String nsstId;

    private SliceSubnetType sliceSubnetType;


    private UUID nsiId;

    public UUID getNsiId() {
        return nsiId;
    }

    public NetworkSliceSubnetRecordStatus getStatus() {
        return status;
    }

    public void setStatus(NetworkSliceSubnetRecordStatus status) {
        this.status = status;
    }





    public NetworkSliceSubnetInstanceRecord() {
    }

    public NetworkSliceSubnetInstanceRecord(String nsstId, UUID nssiIdentifier, UUID nsiId, NetworkSliceSubnetRecordStatus status, SliceSubnetType sliceSubnetType) {

        this.nsstId = nsstId;
        this.status= status;
        this.nssiIdentifier = nssiIdentifier;
        this.nsiId = nsiId;
        this.sliceSubnetType = sliceSubnetType;

    }

    public SliceSubnetType getSliceSubnetType() {
        return sliceSubnetType;
    }

    public UUID getNssiIdentifier() {
        return nssiIdentifier;
    }

    public String getNsstId() {
        return nsstId;
    }

    public NetworkSliceSubnetInstance getNetworkSliceSubnetInstance(){
        return new NetworkSliceSubnetInstance(this.nssiIdentifier, status.asNsiStatus(), this.nsstId, sliceSubnetType.toString());
    }
}
