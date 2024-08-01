package com.example.pwc.service

import com.example.pwc.model.Country
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.springframework.core.io.ClassPathResource
import org.springframework.core.io.Resource
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service
import org.springframework.web.server.ResponseStatusException
import java.io.File
import java.util.LinkedList
import java.util.Queue


const val COUNTRY_JSON_PATH = "countries.json"

@Service
class RoutingService(
    private var mapOfCountries: MutableMap<String, List<String>>
) {
    init {
        val resource: Resource = ClassPathResource(COUNTRY_JSON_PATH)
        val file: File = resource.file
        
        val jsonString = file.readText()
        val typeToken = object : TypeToken<List<Country>>() {}.type
        val countriesAndTheirNeighbours = Gson().fromJson<List<Country>>(jsonString, typeToken)

        for (country in countriesAndTheirNeighbours) {
            mapOfCountries[country.name] = country.neighbours
        }
    }
    
    private fun processCountryGraphBreadthFirst(processingQueue: Queue<String>, processedAlready: MutableList<String>,
                                                destinationCountry: String): Map<String, String> {
        // Create a map of <child, parent> pair, so we can know how did we reach the destination
        val parentMap: MutableMap<String, String> = mutableMapOf()
        // Put the source country as having no parent since we start with it
        parentMap[processingQueue.peek()] = ""
        while (!processingQueue.isEmpty()) {
            val currentlyProcessing = processingQueue.remove()
            processedAlready.add(currentlyProcessing)
            if (mapOfCountries[currentlyProcessing]?.contains(destinationCountry) == true) {
                parentMap[destinationCountry] = currentlyProcessing
                return parentMap
            }
            val notVisitedAndNotProcessedBorders = mapOfCountries[currentlyProcessing]?.minus(processedAlready.toSet())?.minus(processingQueue)
            notVisitedAndNotProcessedBorders?.forEach { parentMap[it] = currentlyProcessing }
            notVisitedAndNotProcessedBorders?.let { processingQueue.addAll(it) }
        }
        return emptyMap()
    }
    
    fun computeShortestRoute(sourceCountry: String, destinationCountry: String): List<String> {
        if (mapOfCountries[sourceCountry] == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Country $sourceCountry not found!")
        }
        if (mapOfCountries[destinationCountry] == null) {
            throw ResponseStatusException(HttpStatus.NOT_FOUND, "Country $destinationCountry not found!")
        }
        val processedAlready: MutableList<String> = mutableListOf()
        
        val processingList: Queue<String> = LinkedList()
        processingList.add(sourceCountry)
        
        val solutionMap = processCountryGraphBreadthFirst(processingList, processedAlready, destinationCountry)
        var solution: MutableList<String> = mutableListOf()
        if (solutionMap.isNotEmpty()) {
            solution = buildPathFromParentChildMap(destinationCountry, sourceCountry, solutionMap)
        }
        if (solution.isEmpty()) {
            throw ResponseStatusException(HttpStatus.BAD_REQUEST, "No available land route between $sourceCountry and $destinationCountry")

        }
        return solution
    }

    private fun buildPathFromParentChildMap(
        destinationCountry: String,
        sourceCountry: String,
        solutionMap: Map<String, String>
    ): MutableList<String> {
        val solution: MutableList<String> = mutableListOf()
        var currentParent = destinationCountry
        while (currentParent != sourceCountry) {
            solution.add(currentParent)
            currentParent = solutionMap[currentParent].toString()
        }
        solution.add(currentParent)
        solution.reverse()
        return solution
    }
}