package com.example.camel.service;

import com.example.camel.dto.TrainDTO;
import io.protostuff.LinkedBuffer;
import io.protostuff.ProtobufIOUtil;
import io.protostuff.runtime.RuntimeSchema;
import org.apache.camel.Exchange;
import org.springframework.stereotype.Component;

@Component
public class ProtobufService {

    public byte[] convertDtoToProtobuf(Exchange exchange) {
        TrainDTO trainDto = exchange.getIn().getBody(TrainDTO.class);
        RuntimeSchema<TrainDTO> runtimeSchema = RuntimeSchema.createFrom(TrainDTO.class);
        LinkedBuffer linkedBuffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            byte[]  toProtobuf = ProtobufIOUtil.toByteArray(trainDto, runtimeSchema, linkedBuffer);
            return toProtobuf;
        } finally {
            linkedBuffer.clear();
        }
    }
}
