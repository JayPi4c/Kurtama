package org.schlunzis.kurtama.common;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import com.fasterxml.jackson.databind.annotation.JsonTypeIdResolver;
import org.schlunzis.kurtama.common.util.InheritanceTypeIdResolver;

import java.util.UUID;

@JsonTypeInfo(use = JsonTypeInfo.Id.CUSTOM, property = "discriminator")
@JsonTypeIdResolver(InheritanceTypeIdResolver.class)
public interface IUser {

    UUID getId();

    String getUsername();

    void setUsername(String username);

    UserDTO toDTO();

}
