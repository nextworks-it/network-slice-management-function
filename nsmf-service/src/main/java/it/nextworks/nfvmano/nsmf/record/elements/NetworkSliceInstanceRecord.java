package it.nextworks.nfvmano.nsmf.record.elements;

import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceInstance;

import javax.persistence.*;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Entity
public class NetworkSliceInstanceRecord {
    @Id
    @GeneratedValue
    private UUID id;

    @ManyToMany
    private Set<NetworkSliceSubnetInstanceRecord> networkSliceSubnetInstances;

    private String nstId;

    private String vsInstanceId;

    private NetworkSliceInstanceRecordStatus status;

    private String tenantId;

    private String errorMsg;


    public NetworkSliceInstanceRecord() {
    }

    public NetworkSliceInstanceRecord(Set<NetworkSliceSubnetInstanceRecord> networkSliceSubnetInstances,
                                      String nstId,
                                      String vsInstanceId,
                                      NetworkSliceInstanceRecordStatus status,
                                      String tenantId) {
        this.networkSliceSubnetInstances = networkSliceSubnetInstances;
        this.nstId = nstId;
        this.vsInstanceId = vsInstanceId;
        this.status=status;
        this.tenantId= tenantId;
    }

    public Set<NetworkSliceSubnetInstanceRecord> getNetworkSliceSubnetInstanceIds() {
        return networkSliceSubnetInstances;
    }

    public NetworkSliceInstanceRecordStatus getStatus() {
        return status;
    }

    public String getTenantId() {
        return tenantId;
    }

    public String getNstId() {
        return nstId;
    }

    public String getVsInstanceId() {
        return vsInstanceId;
    }

    public UUID getId() {
        return id;
    }

    public NetworkSliceInstance getNetworkSliceInstance(){
        return new NetworkSliceInstance(this.id, this.getNetworkSliceSubnetInstanceIds().stream()
                .map(e -> e.getId())
                .collect(Collectors.toList()), vsInstanceId, status.asNsiStatus(),status.toString()  );
    }


    public void setStatus(NetworkSliceInstanceRecordStatus status) {
        this.status = status;
    }

    public String getErrorMsg() {
        return errorMsg;
    }

    public void setErrorMsg(String errorMsg) {
        this.errorMsg = errorMsg;
    }
}
