package org.schlunzis.kurtama.server.service;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import org.schlunzis.kurtama.common.messages.IServerMessage;
import org.schlunzis.kurtama.server.auth.IAuthenticationService;
import org.schlunzis.kurtama.server.net.ISession;
import org.schlunzis.kurtama.server.user.ServerUser;
import org.springframework.core.ResolvableType;
import org.springframework.core.ResolvableTypeProvider;

@Getter
@ToString
@EqualsAndHashCode(callSuper = true)
public class SecondaryRequestContext<S extends IServerMessage> extends AbstractMessageContext implements ResolvableTypeProvider {

    private final S mainResponse;

    SecondaryRequestContext(S mainResponse, ISession session, ServerUser user, ResponseAssembler responseAssembler,
                            IAuthenticationService authenticationService) {
        super(responseAssembler, authenticationService, session, user);
        this.mainResponse = mainResponse;
    }

    @Override
    public ResolvableType getResolvableType() {
        return ResolvableType.forClassWithGenerics(
                getClass(),
                ResolvableType.forInstance(this.mainResponse)
        );
    }

}
