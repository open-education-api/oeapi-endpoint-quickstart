/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package oeapi.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;

/**
 *
 * @author itziar.urrutia
 */
public interface oeapiDTOControllerInterface<T, S> {

    public ResponseEntity<?> createFromDTO(@RequestBody S dto);

    public ResponseEntity<?> updateFromDTO(@PathVariable String courseId, @RequestBody S dto);
}
