package dev.joaolevy.leads.common;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/** Transforma exceções em respostas JSON claras, com o código HTTP certo. */
@RestControllerAdvice
public class TratadorDeErros {

    @ExceptionHandler(RecursoNaoEncontradoException.class)
    ResponseEntity<ErroResponse> naoEncontrado(RecursoNaoEncontradoException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ErroResponse.de(404, e.getMessage()));
    }

    @ExceptionHandler(ConflitoException.class)
    ResponseEntity<ErroResponse> conflito(ConflitoException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ErroResponse.de(409, e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ErroResponse> validacao(MethodArgumentNotValidException e) {
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError erro : e.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(erro.getField(), erro.getDefaultMessage());
        }
        return ResponseEntity.badRequest()
                .body(new ErroResponse(LocalDateTime.now(), 400, "Dados inválidos", campos));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ErroResponse> jsonInvalido(HttpMessageNotReadableException e) {
        return ResponseEntity.badRequest().body(ErroResponse.de(400, "JSON inválido ou valor desconhecido"));
    }
}
