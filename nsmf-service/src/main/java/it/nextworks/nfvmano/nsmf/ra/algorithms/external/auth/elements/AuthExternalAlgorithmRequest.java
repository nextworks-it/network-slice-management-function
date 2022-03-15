package it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.elements;

import it.nextworks.nfvmano.libs.vs.common.ra.elements.ExternalAlgorithmRequest;

import java.util.List;
import java.util.Map;

public class AuthExternalAlgorithmRequest extends ExternalAlgorithmRequest {

    private List<Node> nodeList;
    private List<Link> linkList;
    private List<Vnf> vnfs;
    private List<Sfc> sfcList;
    private E2EQoS e2eQoS;
    private Map<String, Float> pnfParameters;

    public AuthExternalAlgorithmRequest(){}

    public AuthExternalAlgorithmRequest(String requestId,
                                        List<Node> nodes, List<Link> linkList,
                                        List<Vnf> vnfs, List<Sfc> sfcList,
                                        E2EQoS e2eQoS, Map<String, Float> pnfParameters){
        this.setRequestId(requestId);
        this.nodeList =nodes;
        this.linkList=linkList;
        this.vnfs=vnfs;
        this.sfcList=sfcList;
        this.e2eQoS=e2eQoS;
        this.pnfParameters=pnfParameters;
    }

    public List<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(List<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public List<Link> getLinkList() {
        return linkList;
    }

    public void setLinkList(List<Link> linkList) {
        this.linkList = linkList;
    }

    public List<Vnf> getVnfs() {
        return vnfs;
    }

    public void setVnfs(List<Vnf> vnfs) {
        this.vnfs = vnfs;
    }

    public List<Sfc> getSfcList() {
        return sfcList;
    }

    public void setSfcList(List<Sfc> sfcList) {
        this.sfcList = sfcList;
    }

    public E2EQoS getE2eQoS() {
        return e2eQoS;
    }

    public void setE2eQoS(E2EQoS e2eQoS) {
        this.e2eQoS = e2eQoS;
    }

    public Map<String, Float> getPnfParameters() {
        return pnfParameters;
    }

    public void setPnfParameters(Map<String, Float> pnfParameters) {
        this.pnfParameters = pnfParameters;
    }
}
