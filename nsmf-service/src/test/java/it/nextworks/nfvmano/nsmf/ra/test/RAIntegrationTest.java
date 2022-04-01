package it.nextworks.nfvmano.nsmf.ra.test;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;

import it.nextworks.nfvmano.libs.vs.common.ra.messages.compute.ResourceAllocationComputeRequest;
import it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.driver.AuthRequestTranslator;
import it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.elements.AuthExternalAlgorithmRequest;
import org.junit.Test;

import java.io.File;

public class RAIntegrationTest {

    @Test
    public void AuthIntegrationTranslationTest(){
        try{
            ObjectMapper mapper=new ObjectMapper();
            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            ResourceAllocationComputeRequest request=mapper.readValue(new File("/home/nicola/network-slice-management-function/nsmf-service/src/test/resources/request.json"), ResourceAllocationComputeRequest.class);

            AuthRequestTranslator translator=new AuthRequestTranslator();
            AuthExternalAlgorithmRequest ext_req=translator.translateRAComputeRequest(request);

            String result= mapper.writeValueAsString(ext_req);
            System.out.println(result);
        } catch (Exception e){
            e.printStackTrace();
        }
    }
}
