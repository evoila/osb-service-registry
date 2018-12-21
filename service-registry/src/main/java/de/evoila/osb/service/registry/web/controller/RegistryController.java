package de.evoila.osb.service.registry.web.controller;

import de.evoila.osb.service.registry.web.bodies.SimpleResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class RegistryController extends BaseController {

    @GetMapping(value = "/status")
    public ResponseEntity<?> status() throws Exception {
        return new ResponseEntity<SimpleResponse>(new SimpleResponse("Service Registry is running."), HttpStatus.OK);
    }
}
