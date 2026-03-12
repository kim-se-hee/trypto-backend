package ksh.tryptobackend.user.application.service;

import ksh.tryptobackend.common.exception.CustomException;
import ksh.tryptobackend.common.exception.ErrorCode;
import ksh.tryptobackend.user.application.port.in.GetUserProfileUseCase;
import ksh.tryptobackend.user.application.port.in.dto.query.GetUserProfileQuery;
import ksh.tryptobackend.user.application.port.out.UserQueryPort;
import ksh.tryptobackend.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class GetUserProfileService implements GetUserProfileUseCase {

    private final UserQueryPort userQueryPort;

    @Override
    public User getUserProfile(GetUserProfileQuery query) {
        return userQueryPort.findById(query.userId())
            .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}
