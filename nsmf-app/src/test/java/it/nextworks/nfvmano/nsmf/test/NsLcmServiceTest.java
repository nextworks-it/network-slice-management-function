package it.nextworks.nfvmano.nsmf.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.catalogue.template.messages.nst.OnBoardNsTemplateRequest;
import it.nextworks.nfvmano.libs.vs.common.exceptions.*;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.provisioning.CreateNsiIdRequest;
import it.nextworks.nfvmano.libs.vs.common.nsmf.messages.provisioning.InstantiateNsiRequest;
import it.nextworks.nfvmano.nsmf.NsLcmService;
import it.nextworks.nfvmano.nsmf.NsmfApplication;
import it.nextworks.nfvmano.nsmf.manager.NsLcmManager;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;
import it.nextworks.nfvmano.catalogue.template.interfaces.NsTemplateCatalogueInterface;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = NsmfApplication.class)

public class NsLcmServiceTest {

    @Autowired
    NsLcmService nsLcmService;

    @Autowired
    private NsTemplateCatalogueInterface nsTemplateCatalogueInterface;

    @Test
    public void testCreateNsIdentifier() throws FailedOperationException,
            MethodNotImplementedException, NotExistingEntityException, MalformattedElementException, NotPermittedOperationException {


        try {
            onboardNST();
            nsLcmService.createNetworkSliceIdentifierFromNst(new CreateNsiIdRequest(
                            "5gcroco_acca_e2e",
                            "testNsName",
                            "testNsDescription",
                            null),
                    "admin");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException e) {
            e.printStackTrace();
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.AlreadyExistingEntityException e) {
            e.printStackTrace();
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.MethodNotImplementedException e) {
            e.printStackTrace();
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException e) {
            e.printStackTrace();
        }


    }


    private void onboardNST() throws it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException, it.nextworks.nfvmano.libs.ifa.common.exceptions.AlreadyExistingEntityException, it.nextworks.nfvmano.libs.ifa.common.exceptions.MethodNotImplementedException, it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException, IOException {
        String resourceName = "OnboardNstEndToEnd.json";

        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(resourceName).getFile());
        String absolutePath = file.getAbsolutePath();

        System.out.println(absolutePath);

        ObjectMapper mapper = new ObjectMapper();

            OnBoardNsTemplateRequest onBoardNsTemplateRequest = mapper.readValue(file, OnBoardNsTemplateRequest.class);
            nsTemplateCatalogueInterface.onBoardNsTemplate(onBoardNsTemplateRequest);
    }

    @Test
    public void testInstantiateNs() throws FailedOperationException,
            MethodNotImplementedException, NotExistingEntityException, MalformattedElementException, NotPermittedOperationException {
        try {
            onboardNST();
            UUID nsiId = nsLcmService.createNetworkSliceIdentifierFromNst(new CreateNsiIdRequest(
                            "nst_test_e2e",
                            "testNsName",
                            "testNsDescription",
                            null),
                    "admin");
            nsLcmService.instantiateNetworkSlice(new InstantiateNsiRequest(nsiId), "admin");
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.FailedOperationException e) {
            e.printStackTrace();
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.AlreadyExistingEntityException e) {
            e.printStackTrace();
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.MethodNotImplementedException e) {
            e.printStackTrace();
        } catch (it.nextworks.nfvmano.libs.ifa.common.exceptions.MalformattedElementException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }



    }
}
