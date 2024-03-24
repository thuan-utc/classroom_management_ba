package utc.k61.cntt2.class_management.security;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import utc.k61.cntt2.class_management.domain.User;
import utc.k61.cntt2.class_management.exception.SomethingNotFoundException;
import utc.k61.cntt2.class_management.repository.UserRepository;

import javax.transaction.Transactional;

@Service
public class CustomerUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Autowired
    public CustomerUserDetailService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String userName)
            throws UsernameNotFoundException {
        User user = userRepository.findByUsername(userName)
                .orElseThrow(() ->
                        new UsernameNotFoundException("User not found with username: " + userName)
                );

        return UserPrincipal.create(user);
    }

    @Transactional
    public UserDetails loadUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(
                () -> new SomethingNotFoundException("Not found user with id = " + id)
        );

        return UserPrincipal.create(user);
    }
}
