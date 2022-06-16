package it.nextworks.nfvmano.nsmf.sbi;

import it.nextworks.nfvmano.libs.ifa.templates.nst.NSST;
import it.nextworks.nfvmano.libs.vs.common.nssmf.interfaces.NssmfLcmProvisioningInterface;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.record.NsiRecordService;
import it.nextworks.nfvmano.nsmf.sbi.dummy.DummyNssmfClient;
import it.nextworks.nfvmano.nsmf.sbi.specific.AppNssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.specific.CoreNssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.specific.OsmNssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.specific.TransportNssmfRestClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class NssmfDriverRegistry {


    @Value("${nssmf.plugin.core.address:http://localhost:8085}")
    private String corePluginAddress;

    @Value("${nssmf.plugin.transport.address:http://localhost:8086}")
    private String transportPluginAddress;

    @Value("${nssmf.plugin.edge.address:http://localhost:8087}")
    private String edgePluginAddress;

    @Value("${nssmf.plugin.ran.address:http://localhost:8089}")
    private String ranPluginAddress;

    @Value("${nssmf.plugin.vapp.address:http://localhost:8088}")
    private String vappPluginAddress;

    @Value("${nssmf.plugin.dummy.address:http://localhost}")
    private String dummyPluginAddress;

    @Value("${nssmf.plugin.osm.address:http://localhost}")
    private String osmPluginAddress;

    @Value("${nssmf.type:STANDARD}")
    private String nssmfType;

    @Autowired
    private NsiRecordService nsiRecordService;
    public NssmfLcmProvisioningInterface getNssmfLcmDriver(ResourceAllocationComputeResponse em, NSST targetNsst){

        switch (nssmfType){
            case "STANDARD":
                switch (targetNsst.getType()) {
                    case RAN:
                        return null;
                    case TRANSPORT:
                        return new TransportNssmfRestClient(transportPluginAddress, nsiRecordService);
                    case CORE:
                        return new CoreNssmfRestClient(corePluginAddress);
                    case VAPP:
                        return new AppNssmfRestClient(vappPluginAddress);
                    default:
                        return null;
                }
            case "OSM":
                return new OsmNssmfRestClient(osmPluginAddress);
            case "DUMMY":
                return new DummyNssmfClient(dummyPluginAddress);
            default:
                return null;
        }
    }
}
