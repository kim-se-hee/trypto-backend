package ksh.tryptobackend.user.adapter.out;

import ksh.tryptobackend.user.adapter.out.entity.UserJpaEntity;
import ksh.tryptobackend.user.adapter.out.repository.UserJpaRepository;
import ksh.tryptobackend.user.application.port.out.UserCommandPort;
import ksh.tryptobackend.user.domain.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserCommandAdapter implements UserCommandPort {

    private final UserJpaRepository userJpaRepository;

    @Override
    public User save(User user) {
        UserJpaEntity saved = userJpaRepository.save(UserJpaEntity.fromDomain(user));
        return saved.toDomain();
    }
}
