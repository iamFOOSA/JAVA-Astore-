package by.abram.astore.service;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.UserDto;
import by.abram.astore.entity.User;
import by.abram.astore.exception.ResourceNotFoundException;
import by.abram.astore.mapper.UserMapper;
import by.abram.astore.repository.CartRepository;
import by.abram.astore.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;


@Service
@RequiredArgsConstructor
public class UserService {

    private static final String DEFAULT_DEMO_PASSWORD = "demo-password";

    private final UserRepository userRepository;
    private final CartRepository cartRepository;
    private final UserMapper userMapper;
    private final ProductCacheService productCacheService;

    @Transactional
    public UserDto create(UserDto dto) {
        User user = userMapper.toEntity(dto);
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            user.setPassword(DEFAULT_DEMO_PASSWORD);
        }
        User savedUser = userRepository.save(user);

        productCacheService.invalidateCache();

        return userMapper.toDto(savedUser);
    }

    @Transactional
    public UserDto update(Long id, UserDto dto) {
        User existingUser = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("пользователь не найден с id: " + id));

        existingUser.setEmail(dto.getEmail());
        existingUser.setFirstName(dto.getFirstName());
        existingUser.setLastName(dto.getLastName());

        User updatedUser = userRepository.save(existingUser);

        productCacheService.invalidateCache();

        return userMapper.toDto(updatedUser);
    }

    @Transactional(readOnly = true)
    public UserDto findById(Long id) {
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    @Transactional(readOnly = true)
    public Page<UserDto> findAll(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(userMapper::toDto);
    }

    @Transactional
    public void delete(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("пользователь не найден с id: " + id);
        }
        cartRepository.deleteByUserId(id);
        userRepository.deleteById(id);

        productCacheService.invalidateCache();
    }
}
