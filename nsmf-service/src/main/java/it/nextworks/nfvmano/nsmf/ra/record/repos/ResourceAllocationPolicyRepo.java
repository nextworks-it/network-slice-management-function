package it.nextworks.nfvmano.nsmf.ra.record.repos;

import it.nextworks.nfvmano.nsmf.ra.record.elements.ResourceAllocationPolicyRecord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ResourceAllocationPolicyRepo  extends JpaRepository<ResourceAllocationPolicyRecord, Long> {
}
