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
        RuntimeSchema<TrainDTO> schema = RuntimeSchema.createFrom(TrainDTO.class);
        LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);
        try {
            byte[] protobuff = ProtobufIOUtil.toByteArray(trainDto, schema, buffer);
            return protobuff;
        } finally {
            buffer.clear();
        }
    }
}
