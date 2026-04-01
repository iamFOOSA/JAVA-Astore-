package by.abram.astore.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String resourceName, Object fieldValue) {
        super(String.format("%s не найден с id : %s", resourceName, fieldValue));
    }
}