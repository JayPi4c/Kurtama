package org.schlunzis.kurtama.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO implements IUser {

    private UUID id;

    private String username;

    @Override
    public UserDTO toDTO() {
        return this;
    }

}
