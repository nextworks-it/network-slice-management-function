package it.nextworks.nfvmano.nsmf.nbi;

import it.nextworks.nfvmano.libs.vs.common.vsmf.interfaces.VsmfNotificationInterface;
import it.nextworks.nfvmano.libs.vs.common.vsmf.message.VsmfNotificationMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;

@Service
public class VsmfNotifier implements VsmfNotificationInterface {

    private static final Logger log = LoggerFactory.getLogger(VsmfNotifier.class);

    @Value("${nsmf.vsmfnotifier.auth.user}")
    private String username;
    @Value("${nsmf.vsmfnotifier.auth.pwd}")
    private String password;
    @Value("${nsmf.vsmfnotifier.notifyUrl}")
    private String notifyUrl;

    @Value("${nsmf.vsmfnotifier.loginUrl:noLogin}")
    private String loginUrl;

    private VsmfRestClient vsmfRestClient;

    @PostConstruct
    private void initRestClient(){
        vsmfRestClient = new VsmfRestClient(notifyUrl, loginUrl);
    }

    @Override
    public void notifyVsmf(VsmfNotificationMessage message){
        log.debug("Sending NSI Status change notification");
        log.debug("Requested by " + message.getNsiId().toString());

        boolean authenticated;
        if (loginUrl.equals("noLogin"))
            authenticated=true;
        else
            authenticated=vsmfRestClient.authenticate(username, password);

        if (authenticated){
            vsmfRestClient.notifyVsmf(message);
            log.debug("Notification Sent");
        }else
            log.debug("Error in sending notification to NSMF. Requester: " + message.getNsiId().toString());
    }
}

class VsmfRestClient implements VsmfNotificationInterface{
    private final static Logger log=LoggerFactory.getLogger(VsmfRestClient.class);

    private RestTemplate restTemplate;
    private String notifyUrl;

    private String loginUrl;
    private String authCookie;

    VsmfRestClient(String notifyUrl, String loginUrl){
        this.notifyUrl=notifyUrl;
        this.loginUrl=loginUrl;
        this.restTemplate=new RestTemplate();
    }
    boolean authenticate(String username, String password){
        log.info("Building http request to login");
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("username", username);
        map.add("password", password);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);
        try{
            ResponseEntity<String> httpResponse = restTemplate.exchange(loginUrl, HttpMethod.POST, request, String.class);
            HttpHeaders headersResp = httpResponse.getHeaders();
            HttpStatus code = httpResponse.getStatusCode();

            if (code.equals(HttpStatus.OK)) {
                authCookie = headersResp.getFirst(HttpHeaders.SET_COOKIE);
                log.info("Authentication performed on VSMF. Cookie:  " + authCookie);
                return true;
            }

            log.debug("Error while sending notification - HTTP Code: "+code.toString());
            return false;

        }catch (RestClientException e) {
            log.error("Error during authentication process with the VSMF " + e.toString());
            return false;
        }
    }

    @Override
    public void notifyVsmf(VsmfNotificationMessage vsmfNotificationMessage) {
        HttpHeaders header = new HttpHeaders();
        header.add("Content-Type", "application/json");

        if(authCookie!=null)
            header.add("Cookie", this.authCookie);

        HttpEntity<?> postEntity = new HttpEntity<>(vsmfNotificationMessage, header);

        try {
            log.debug("Sending HTTP message to notify network slice status change.");
            ResponseEntity<String> httpResponse =
                    restTemplate.exchange(notifyUrl, HttpMethod.POST, postEntity, String.class);

            log.debug("Response code: " + httpResponse.getStatusCode().toString());
            HttpStatus code = httpResponse.getStatusCode();

            if (code.equals(HttpStatus.OK)) {
                log.debug("Notification correctly dispatched.");
            } else {
                log.debug("Error while sending notification");
            }
        } catch (RestClientException e) {
            log.debug("Error while sending notification");
            log.debug(e.toString());
            log.debug("RestClientException response: Message " + e.getMessage());
        }
    }
}