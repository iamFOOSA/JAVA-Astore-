package by.abram.astore;

import by.abram.astore.cache.ProductCacheService;
import by.abram.astore.dto.UserDto;
import by.abram.astore.entity.User;
import by.abram.astore.exception.ResourceNotFoundException;
import by.abram.astore.mapper.UserMapper;
import by.abram.astore.repository.UserRepository;
import by.abram.astore.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private UserRepository userRepository;
    @Mock private UserMapper userMapper;
    @Mock private ProductCacheService productCacheService;

    @InjectMocks private UserService userService;

    @Test
    void create_ShouldReturnUserDto() {
        UserDto dto = new UserDto();
        User user = new User();
        when(userMapper.toEntity(dto)).thenReturn(user);
        when(userRepository.save(user)).thenReturn(user);
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.create(dto);

        assertNotNull(result);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void update_ShouldUpdateAndReturnDto() {
        Long id = 1L;
        UserDto dto = new UserDto();
        dto.setEmail("test@mail.com");
        User existingUser = new User();

        when(userRepository.findById(id)).thenReturn(Optional.of(existingUser));
        when(userRepository.save(any(User.class))).thenReturn(existingUser);
        when(userMapper.toDto(existingUser)).thenReturn(dto);

        userService.update(id, dto);

        assertEquals("test@mail.com", dto.getEmail());
        verify(productCacheService).invalidateCache();
    }

    @Test
    void update_ShouldThrowException_WhenUserNotFound() {
        Long userId = 1L;
        UserDto dto = new UserDto();
        when(userRepository.findById(userId)).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.update(userId, dto));
    }

    @Test
    void findById_ShouldReturnDto() {
        User user = new User();
        UserDto dto = new UserDto();
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userMapper.toDto(user)).thenReturn(dto);

        UserDto result = userService.findById(1L);

        assertNotNull(result);
    }

    @Test
    void findById_ShouldThrowResourceNotFound() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> userService.findById(1L));
    }

    @Test
    void findAll_ShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 5);
        Page<User> page = new PageImpl<>(List.of(new User()));
        when(userRepository.findAll(pageable)).thenReturn(page);
        when(userMapper.toDto(any())).thenReturn(new UserDto());

        Page<UserDto> result = userService.findAll(0, 5);

        assertEquals(1, result.getTotalElements());
    }

    @Test
    void delete_ShouldInvokeRepository() {
        when(userRepository.existsById(1L)).thenReturn(true);
        userService.delete(1L);
        verify(userRepository).deleteById(1L);
        verify(productCacheService).invalidateCache();
    }

    @Test
    void delete_ShouldThrow_WhenNotExists() {
        when(userRepository.existsById(1L)).thenReturn(false);
        assertThrows(EntityNotFoundException.class, () -> userService.delete(1L));
    }
}