package it.nextworks.nfvmano.nsmf.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.libs.vs.common.exceptions.AlreadyExistingEntityException;
import it.nextworks.nfvmano.libs.vs.common.exceptions.MalformattedElementException;
import it.nextworks.nfvmano.libs.vs.common.ra.messages.policy.OnboardRAPolicyRequest;
import it.nextworks.nfvmano.nsmf.NsmfApplication;
import it.nextworks.nfvmano.nsmf.ra.ResourceAllocationPolicyService;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.io.File;
import java.io.IOException;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NsmfApplication.class)

public class TestRAPolicyMgmt {


    @Autowired
    private ResourceAllocationPolicyService resourceAllocationPolicyService;

    @Test
    public void testOnboardRAPolicy(){
        String resourceName = "OnboardRAPolicyRequest.json";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());
        String absolutePath = file.getAbsolutePath();

        System.out.println(absolutePath);

        ObjectMapper mapper = new ObjectMapper();
        try {
            OnboardRAPolicyRequest raPolicyRequest = mapper.readValue(file, OnboardRAPolicyRequest.class);
            resourceAllocationPolicyService.onboardResourceAllocationPolicy(raPolicyRequest);
        } catch (IOException | MalformattedElementException | AlreadyExistingEntityException e) {
            e.printStackTrace();
        }
    }
}
