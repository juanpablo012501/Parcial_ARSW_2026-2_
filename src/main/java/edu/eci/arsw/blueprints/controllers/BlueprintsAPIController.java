package edu.eci.arsw.blueprints.controllers;

import edu.eci.arsw.blueprints.model.Blueprint;
import edu.eci.arsw.blueprints.model.Point;
import edu.eci.arsw.blueprints.persistence.BlueprintNotFoundException;
import edu.eci.arsw.blueprints.persistence.BlueprintPersistenceException;
import edu.eci.arsw.blueprints.services.BlueprintsServices;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.apache.coyote.Response;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/v1/blueprints")
public class BlueprintsAPIController {

    private final BlueprintsServices services;

    public BlueprintsAPIController(BlueprintsServices services) { this.services = services; }

    // GET /blueprints
    @GetMapping
    @Operation(summary = "gets the list of all blueprints")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "successful query")
    })
    public ResponseEntity<ApiResult<Set<Blueprint>>> getAll() {
        Set<Blueprint> all = services.getAllBlueprints();
        return ResponseEntity.ok(new ApiResult<>(200, "successful query", all));
    }

    // GET /blueprints/{author}
    @GetMapping("/{author}")
    @Operation(summary = "gets all the author's blueprints")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "successful query"),
            @ApiResponse(responseCode = "404", description = "author not found")
    })
    public ResponseEntity<ApiResult<Set<Blueprint>>> byAuthor(@PathVariable String author) {
        try {
            return ResponseEntity.ok(new ApiResult<>(200, "successful query", services.getBlueprintsByAuthor(author)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResult<>(404, "author not found", null));
        }
    }

    // GET /blueprints/{author}/{bpname}
    @GetMapping("/{author}/{bpname}")
    @Operation(summary = "get the author's blueprint with the name given")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "successful query"),
            @ApiResponse(responseCode = "404", description = "author or blueprint not found")
    })
    public ResponseEntity<ApiResult<Blueprint>> byAuthorAndName(@PathVariable String author, @PathVariable String bpname) {
        try {
            return ResponseEntity.ok(new ApiResult<>(200, "successful query", services.getBlueprint(author, bpname)));
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResult<>(404, "author or blueprint not found", null));
        }
    }

    // POST /blueprints
    @PostMapping
    @Operation(summary = "create a new blueprint")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "blueprint created"),
            @ApiResponse(responseCode = "400", description = "invalid data")
    })
    public ResponseEntity<?> add(@Valid @RequestBody NewBlueprintRequest req) {
        try {
            Blueprint bp = new Blueprint(req.author(), req.name(), req.points());
            services.addNewBlueprint(bp);
            return ResponseEntity.status(HttpStatus.CREATED).build();
        } catch (BlueprintPersistenceException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(new ApiResult<>(400, "invalid data", null));
        }
    }

    // PUT /blueprints/{author}/{bpname}/points
    @PutMapping("/{author}/{bpname}/points")
    @Operation(summary = "author updates the blueprint")
    @ApiResponses({
            @ApiResponse(responseCode = "202", description = "accepted change"),
            @ApiResponse(responseCode = "400", description = "invalid data")
    })
    public ResponseEntity<?> addPoint(@PathVariable String author, @PathVariable String bpname,
                                      @RequestBody Point p) {
        try {
            services.addPoint(author, bpname, p.x(), p.y());
            return ResponseEntity.status(HttpStatus.ACCEPTED).build();
        } catch (BlueprintNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ApiResult<>(400, "invalid data", null));
        }
    }

    public record NewBlueprintRequest(
            @NotBlank String author,
            @NotBlank String name,
            @Valid java.util.List<Point> points
    ) { }
}
