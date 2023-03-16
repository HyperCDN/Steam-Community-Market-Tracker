package de.hypercdn.scmt.error

import jakarta.servlet.ServletException
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageConversionException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.servlet.NoHandlerFoundException

@RestControllerAdvice
@ResponseBody
class GeneralErrorHandler {

    @ExceptionHandler
    @ResponseStatus(HttpStatus.NOT_FOUND)
    fun handleNotFound(exception: NoHandlerFoundException) {
    }

    @ExceptionHandler
    fun handleResponseStatusException(exception: ResponseStatusException, response: HttpServletResponse) {
        throw exception
    }

    @ExceptionHandler(HttpMessageConversionException::class, ServletException::class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    fun handleUnsatisfiedServletRequestParameterException() {
    }

    @ExceptionHandler
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    fun handleInternalError(exception: Error) {
    }

}