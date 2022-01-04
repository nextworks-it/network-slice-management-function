package it.nextworks.nfvmano.nsmf.ra.record.converters;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import it.nextworks.nfvmano.libs.vs.common.ra.elements.ResourceAllocationPolicy;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;

@Converter
public class RAPolicyConverter implements AttributeConverter<ResourceAllocationPolicy, String> {

    @Override
    public String convertToDatabaseColumn(ResourceAllocationPolicy resourceAllocationPolicy) {
        String json;
        try{
            ObjectMapper mapper = new ObjectMapper();
            json = mapper.writeValueAsString(resourceAllocationPolicy);
        }
        catch (NullPointerException | JsonProcessingException ex)
        {
            //extend error handling here if you want
            json = "";
        }
        return json;
    }

    @Override
    public ResourceAllocationPolicy convertToEntityAttribute(String s) {
        ObjectMapper mapper = new ObjectMapper();

        try {
            return mapper.readValue(s, ResourceAllocationPolicy.class);
        } catch (IOException e) {
            return null;
        }
    }
}
