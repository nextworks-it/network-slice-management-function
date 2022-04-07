package it.nextworks.nfvmano.nsmf.nfvcatalogue;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import it.nextworks.nfvmano.libs.descriptors.sol006.Nsd;
import it.nextworks.nfvmano.libs.descriptors.sol006.Vnfd;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class NfvoCatalogueClient {

    private ObjectMapper mapper;

    public NfvoCatalogueClient(){
        this.mapper=new ObjectMapper().enable(SerializationFeature.INDENT_OUTPUT);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    }

    public Nsd getNsdById(String nsdId){
        Nsd nsd=null;
        try {
            nsd = mapper.readValue(new File("/home/nicola/network-slice-management-function/nsmf-app/src/main/resources/nsds/" + nsdId + ".json"), Nsd.class);
        } catch (IOException e){
            e.printStackTrace();
        }
        return nsd;
    }

    public Vnfd getVnfdById(String vnfdId){
        Vnfd vnfd=null;
        try{
            vnfd =mapper.readValue(new File("/home/nicola/network-slice-management-function/nsmf-app/src/main/resources/vnfds/"+vnfdId+".json"), Vnfd.class);
        } catch (IOException e){
            e.printStackTrace();
        }
        return vnfd;
    }
}
