package br.com.novaalianca.mnss.onlineapp.security.auth;

import br.com.novaalianca.mnss.onlineapp.domain.user.OnlineUserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OnlineUserDetailsService implements UserDetailsService {

    private final OnlineUserRepository userRepository;

    public OnlineUserDetailsService(OnlineUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return userRepository.findByUsername(username)
                .filter(u -> u.isActive())
                .map(OnlineUserDetails::new)
                .orElseThrow(() -> new UsernameNotFoundException("User not found or inactive: " + username));
    }
}
