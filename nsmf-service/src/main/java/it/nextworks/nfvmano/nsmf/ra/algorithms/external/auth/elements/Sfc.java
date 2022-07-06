package it.nextworks.nfvmano.nsmf.ra.algorithms.external.auth.elements;

import java.util.List;

public class Sfc {

     private String sfcId;
     private String type;

     private String nsstId;
     private List<String> vnfSequence;

     public Sfc(){}

    public Sfc(String sfcId, String type, List<String> vnfSequence) {
        this.sfcId = sfcId;
        this.type = type;
        this.vnfSequence = vnfSequence;
    }

    public String getSfcId() {
        return sfcId;
    }

    public void setSfcId(String sfcId) {
        this.sfcId = sfcId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getNsstId() {
        return nsstId;
    }

    public void setNsstId(String nsstId) {
        this.nsstId = nsstId;
    }

    public List<String> getVnfSequence() {
        return vnfSequence;
    }

    public void setVnfSequence(List<String> vnfSequence) {
        this.vnfSequence = vnfSequence;
    }
}
