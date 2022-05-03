package it.nextworks.nfvmano.nsmf.sbi;

import it.nextworks.nfvmano.libs.ifa.templates.nst.NSST;
import it.nextworks.nfvmano.libs.ifa.templates.nst.SliceSubnetType;
import it.nextworks.nfvmano.libs.vs.common.nssmf.interfaces.NssmfLcmProvisioningInterface;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeResponse;
import it.nextworks.nfvmano.nsmf.engine.messages.NotifyResourceAllocationResponse;
import it.nextworks.nfvmano.nsmf.record.NsiRecordService;
import it.nextworks.nfvmano.nsmf.sbi.dummy.DummyNssmfClient;
import it.nextworks.nfvmano.nsmf.sbi.specific.AppNssmfRestClient;
import it.nextworks.nfvmano.nsmf.sbi.specific.CoreNssmfRestClient;
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

    @Value("${nssmf.plugin.dummy.address}")
    private String dummyPluginAddress;

    @Value("${nssmf.type:REAL}")
    private String nssmfType;

    @Autowired
    private NsiRecordService nsiRecordService;
    public NssmfLcmProvisioningInterface getNssmfLcmDriver(ResourceAllocationComputeResponse em, NSST targetNsst){

        String address = "";
        if(nssmfType.equals("REAL")) {
            if (targetNsst.getType().equals(SliceSubnetType.CORE)) {
                address = corePluginAddress;
                return new CoreNssmfRestClient(address);
            } else if (targetNsst.getType().equals(SliceSubnetType.TRANSPORT)) {
                address = transportPluginAddress;
                return new TransportNssmfRestClient(address, nsiRecordService);

            } else if (targetNsst.getType().equals(SliceSubnetType.RAN)) {
                address = ranPluginAddress;
                return null;
            } else if (targetNsst.getType().equals(SliceSubnetType.VAPP)) {
                address = vappPluginAddress;
                return new AppNssmfRestClient(address);
            }
        }
        address=dummyPluginAddress;
        return new DummyNssmfClient(address);

    }
}
