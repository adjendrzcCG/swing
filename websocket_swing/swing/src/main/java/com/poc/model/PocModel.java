package com.poc.model;

import com.poc.ValueModel;

import java.io.IOException;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class PocModel {

    public Map<ModelProperties, ValueModel<?>> model = new EnumMap<>(ModelProperties.class);
    private HttpBinService httpBinService = new HttpBinService();
    private EventEmitter eventEmitter;

    public PocModel(EventEmitter eventEmitter) {
        model.put(ModelProperties.TEXT_AREA, new ValueModel<String>(null));
        model.put(ModelProperties.FIRST_NAME, new ValueModel<String>(null));
        model.put(ModelProperties.LAST_NAME, new ValueModel<String>(null));
        model.put(ModelProperties.DATE_OF_BIRTH, new ValueModel<String>(null));
        model.put(ModelProperties.ZIP, new ValueModel<String>(null));
        model.put(ModelProperties.ORT, new ValueModel<String>(null));
        model.put(ModelProperties.STREET, new ValueModel<String>(null));
        model.put(ModelProperties.IBAN, new ValueModel<String>(null));
        model.put(ModelProperties.BIC, new ValueModel<String>(null));
        model.put(ModelProperties.VALID_FROM, new ValueModel<String>(null));
        model.put(ModelProperties.MALE, new ValueModel<Boolean>(null));
        model.put(ModelProperties.FEMALE, new ValueModel<Boolean>(null));
        model.put(ModelProperties.DIVERSE, new ValueModel<Boolean>(null));
        this.eventEmitter = eventEmitter;
    }

    public void action() throws IOException, InterruptedException {
        for(var val : ModelProperties.values()) {
            System.out.println(val.toString() + ": " + model.get(val).getField());
        }
        var data = new HashMap<String, String>();
        for(var val : ModelProperties.values()) {
            data.put(val.toString(), model.get(val).getField().toString());
        }
        var responseBody = httpBinService.post(data);
        if(!responseBody.isEmpty()) {
            eventEmitter.emit(responseBody);
        } else {
            eventEmitter.emit("Failed operation");
        }

    }
}
