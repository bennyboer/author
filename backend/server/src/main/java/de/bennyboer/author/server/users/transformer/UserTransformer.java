package de.bennyboer.author.server.users.transformer;

import de.bennyboer.author.server.users.api.UserDTO;
import de.bennyboer.author.user.User;

public class UserTransformer {

    public static UserDTO toApi(User user) {
        return UserDTO.builder()
                .id(user.getId().getValue())
                .version(user.getVersion().getValue())
                .name(user.getName().getValue())
                .createdAt(user.getCreatedAt())
                .build();
    }

}
