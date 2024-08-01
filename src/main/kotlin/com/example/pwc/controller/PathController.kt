package com.example.pwc.controller

import com.example.pwc.service.RoutingService
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/routing")
class PathController(
    @Autowired
    private val routingService: RoutingService
) {
    @GetMapping("/{sourceCountry}/{destinationCountry}")
    fun computePath(@PathVariable sourceCountry: String, @PathVariable destinationCountry: String): List<String> {
        return routingService.computeShortestRoute(sourceCountry, destinationCountry);
    }
}