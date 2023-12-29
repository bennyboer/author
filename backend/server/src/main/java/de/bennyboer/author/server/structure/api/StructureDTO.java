package de.bennyboer.author.server.structure.api;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.Map;

@Value
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@Jacksonized
public class StructureDTO {

    String id;

    long version;

    String rootNodeId;

    Map<String, NodeDTO> nodes;

}
