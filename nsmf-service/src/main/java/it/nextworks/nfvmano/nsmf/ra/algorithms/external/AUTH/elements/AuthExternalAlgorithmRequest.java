package it.nextworks.nfvmano.nsmf.ra.algorithms.external.AUTH.elements;

import it.nextworks.nfvmano.libs.vs.common.ra.elements.ExternalAlgorithmRequest;
import it.nextworks.nfvmano.libs.vs.common.topology.TopologyLink;
import it.nextworks.nfvmano.libs.vs.common.topology.TopologyNode;

import java.util.List;

public class AuthExternalAlgorithmRequest extends ExternalAlgorithmRequest {

    private List<Vnf> vnfs;
    private List<Sfc> sfcList;
    private E2EQoS e2eQoS;

    public AuthExternalAlgorithmRequest(){}

    public AuthExternalAlgorithmRequest(String requestId, List<TopologyNode> nodes, List<TopologyLink> links, List<Vnf> vnfs, List<Sfc> sfcList, E2EQoS e2eQoS){
        this.setRequestId(requestId);
        this.setNodes(nodes);
        this.setLinks(links);
        this.vnfs=vnfs;
        this.sfcList=sfcList;
        this.e2eQoS=e2eQoS;
    }

    public List<Vnf> getVnfs() {
        return vnfs;
    }

    public void setVnfs(List<Vnf> vnfs) {
        this.vnfs = vnfs;
    }

    public E2EQoS getE2eQoS() {
        return e2eQoS;
    }

    public void setE2eQoS(E2EQoS e2eQoS) {
        this.e2eQoS = e2eQoS;
    }
}
