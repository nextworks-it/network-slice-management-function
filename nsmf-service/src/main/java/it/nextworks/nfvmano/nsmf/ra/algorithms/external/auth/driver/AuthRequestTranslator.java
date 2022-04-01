package it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.driver;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.libs.descriptors.sol006.Nsd;
import it.nextworks.nfvmano.libs.descriptors.sol006.Vnfd;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NSST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.NstServiceProfile;
import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeRequest;
import it.nextworks.nfvmano.libs.vs.common.topology.*;
import it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.elements.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AuthRequestTranslator {
    private static final Logger log = LoggerFactory.getLogger(AuthRequestTranslator.class);
    private AuthExternalAlgorithmRequest authRequest;
    private ObjectMapper mapper;

    public AuthRequestTranslator(){
        this.authRequest=new AuthExternalAlgorithmRequest();
        this.mapper=new ObjectMapper();
    }

    public AuthExternalAlgorithmRequest translateRAComputeRequest(ResourceAllocationComputeRequest request){
        log.debug("Received request to translate resource allocation compute request with ID {} into AUTH request", request.getRequestId());
        authRequest.setRequestId(request.getRequestId());

        setNodes(request.getTopology());
        setLinks(request.getTopology());
        setVnfs(request.getNst(), request.getNsds(), request.getVnfds());
        setSfcs(request.getNst(), request.getNsds());
        setE2EQoS(request.getNst());

        return authRequest;
    }

    public void setNodes(NetworkTopology topology){
        List<Node> nodes=new ArrayList<>();
        ExternalProperties externalProperties =new ExternalProperties();
        try {
            externalProperties = mapper.readValue(new File("/home/nicola/network-slice-management-function/nsmf-service/src/main/resources/externalProperties.json"), ExternalProperties.class);
            System.out.println(mapper.writeValueAsString(externalProperties));
        } catch (IOException e){
            e.printStackTrace();
            log.error("Error during external properties deserialization");
        }

        for(TopologyNode node: topology.getNodes()) {
            Node n = new Node();
            n.setNodeId(node.getNodeId());
            switch (node.getNodeType()) {
                case COMPUTE:
                    n.setType(ExtNodeType.COMPUTE);
                    n.setProcessingCapabilities(((ComputeNode) node).getProcessingCapabilities());
                    break;
                case SWITCH:
                    n.setType(ExtNodeType.SWITCH);
                    break;
                case PNF:
                    if (((Pnf) node).getPnfType().equals(PnfType.gNB))
                        n.setType(ExtNodeType.gNB);
                    else if (((Pnf) node).getPnfType().equals(PnfType.BS))
                        n.setType(ExtNodeType.BS);
                    else
                        n.setType(ExtNodeType.SC);
                    n.setProcessingCapabilities(((Pnf) node).getProcessingCapabilities());
            }
            for(Map<String, Object> entry: externalProperties.getExternalProperties()){
                if((entry.get("nodeId")).toString().equals(node.getNodeId())){
                    Map<String, Object> properties=(Map<String, Object>) entry.get("externalProperties");
                    n.setPosition((Map<String, Double>) properties.get("position"));
                    n.setProcessingInfra((Map<String, Integer>) properties.get("processingInfra"));
                }
            }
            nodes.add(n);
        }
        authRequest.setNodeList(nodes);
    }

    public void setLinks(NetworkTopology topology) {
        List<Link> links=new ArrayList<>();
        List<Node> nodes=authRequest.getNodeList();

        for(TopologyLink l: topology.getLinks()){
            Link link=new Link();
            link.setLinkId(l.getLinkId());
            TopologyNode s=l.getSource();
            TopologyNode d=l.getDestination();
            for(Node n: authRequest.getNodeList())
                if(s.getNodeId().equals(n.getNodeId()))
                    link.setSource(n);
                else if (d.getNodeId().equals(n.getNodeId()))
                    link.setDestination(n);
            if(l.getLinkType().equals(LinkType.WIRED)){
                link.setLinkType(LinkType.WIRED);
                link.setBandwidth(l.getBandwidth());
                link.setDelay(l.getDelay());
            } else
                link.setLinkType(LinkType.WIRELESS);
            links.add(link);
        }

        authRequest.setLinkList(links);
    }

    public void setVnfs(NST nst, List<Nsd> nsdList, List<Vnfd> vnfdList){
        List<Vnf> vnfs=new ArrayList<>();
        Nsd nsd=getVappNsd(nst, nsdList);
        List<String> vnfdIds=nsd.getVnfdId();
        for(String vnfdId: vnfdIds) {
            for (Vnfd vnfd : vnfdList) {
                Vnf vnf = new Vnf();
                if (vnfd.getId().equals(vnfdId)) {
                    vnf.setVnfdId(vnfdId);
                    vnf.setType(vnfd.getProductInfoDescription());
                    System.out.println(vnfd.getProductInfoDescription());
                    vnf.setCpuResources(Integer.valueOf(vnfd.getVirtualComputeDesc().get(0).getVirtualCpu().getNumVirtualCpu()));
                    //Here it has to be added the maximum throughput available
                    vnfs.add(vnf);
                    break;
                }
            }
        }
        authRequest.setVnfs(vnfs);
    }

    public void setSfcs(NST nst, List<Nsd> nsdList){
        List<Sfc> sfcs=new ArrayList<>();
        Nsd nsd=getVappNsd(nst, nsdList);
        Sfc sfc=new Sfc("sfc-"+nsd.getId(), nsd.getName(), nsd.getVnfdId());
        sfcs.add(sfc);
        authRequest.setSfcList(sfcs);
    }

    public void setE2EQoS(NST nst){
        E2EQoS qos=new E2EQoS();
        NstServiceProfile serviceProfile=nst.getNstServiceProfileList().get(0);
        qos.setSfcReq(authRequest.getSfcList().get(0).getSfcId());
        qos.setLatency(serviceProfile.getLatency());
        qos.setSurvivalTime(serviceProfile.getSurvivalTime());
        int throughput=(serviceProfile.getdLThptPerSlice()+serviceProfile.getuLThptPerSlice())/2;
        qos.setThroughput(throughput);
        authRequest.setE2eQoS(qos);
    }

    public Nsd getVappNsd(NST nst, List<Nsd> nsdList){
        List<NSST> nssts=nst.getNsst().getNsstList();
        String nsdId="";
        for(NSST nsst: nssts)
            if(nsst.getType().equals(SliceSubnetType.VAPP)){
                nsdId=nsst.getNsdInfo().getNsdId();
                break;
            }
        for(Nsd nsd: nsdList)
            if(nsd.getId().equals(nsdId))
                return nsd;
        return null;
    }
}
