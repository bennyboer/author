package de.bennyboer.author.server.assets.transformer;

import de.bennyboer.author.assets.Asset;
import de.bennyboer.author.assets.Content;
import de.bennyboer.author.server.assets.api.AssetDTO;

public class AssetTransformer {

    public static AssetDTO toApi(Asset asset) {
        return AssetDTO.builder()
                .id(asset.getId().getValue())
                .version(asset.getVersion().getValue())
                .content(asset.getContent().map(Content::getData).orElse(""))
                .contentType(asset.getContent().map(content -> content.getType().asMimeType()).orElse("text/plain"))
                .createdAt(asset.getCreatedAt())
                .build();
    }

}
