package com.croptalk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Port of `app.get('/api/jokes', (req, res) => res.send(jokes))` in server.js.
 * The source never defines or imports `jokes`, so every request throws a ReferenceError and Express answers 500.
 * That behaviour is preserved here rather than inventing a payload (dummy.js is not wired to this route).
 */
@RestController
public class JokesController {

    @GetMapping("/api/jokes")
    public Object getJokes() {
        throw new IllegalStateException("jokes is not defined");
    }
}
