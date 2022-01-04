package it.nextworks.nfvmano.nsmf.record.elements;

import it.nextworks.nfvmano.libs.vs.common.nsmf.elements.NetworkSliceInstance;

import javax.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
public class NetworkSliceSubnetInstanceRecord {
    @Id
    @GeneratedValue
    private UUID id;



    private String nsstId;


    private UUID internalNssiId;






    public NetworkSliceSubnetInstanceRecord() {
    }

    public NetworkSliceSubnetInstanceRecord(String nsstId) {

        this.nsstId = nsstId;


    }




    public UUID getId() {
        return id;
    }


}
