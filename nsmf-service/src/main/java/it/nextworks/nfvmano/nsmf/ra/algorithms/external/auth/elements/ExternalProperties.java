package it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.elements;

import java.util.List;
import java.util.Map;

/**
 * Support Class to deserialize additionalProperties.json file and set the parameters that are not presents into internal topology
 */
public class ExternalProperties {

    private List<Map<String, Object>> externalProperties;

    public ExternalProperties(){}

    public ExternalProperties(List<Map<String, Object>> externalProperties) {
        this.externalProperties = externalProperties;
    }

    public List<Map<String, Object>> getExternalProperties() {
        return externalProperties;
    }

    public void setExternalProperties(List<Map<String, Object>> externalProperties) {
        this.externalProperties = externalProperties;
    }
}
