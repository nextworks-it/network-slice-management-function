package it.nextworks.nfvmano.nsmf.ra.record.elements;

import it.nextworks.nfvmano.libs.vs.common.ra.elements.ResourceAllocationPolicy;
import it.nextworks.nfvmano.nsmf.ra.record.converters.RAPolicyConverter;

import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class ResourceAllocationPolicyRecord {
    @Id
    @GeneratedValue
    private UUID id;

    @Convert(converter= RAPolicyConverter.class)
    private ResourceAllocationPolicy policy;

    public ResourceAllocationPolicyRecord(ResourceAllocationPolicy policy) {
        this.policy = policy;
    }

    public ResourceAllocationPolicyRecord() {
    }

    public UUID getId() {
        return id;
    }

    public ResourceAllocationPolicy getPolicy() {
        return policy;
    }
}
