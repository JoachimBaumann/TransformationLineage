package sdu.masters;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    private final Neo4jLineageService lineageService;

    public LineageController(Neo4jLineageService lineageService) {
        this.lineageService = lineageService;
    }


    @CrossOrigin("*")
    @GetMapping("/backwards/{datasetId}")
    public String traceBackwards(@PathVariable(name = "datasetId") String datasetId) throws Exception {
        return lineageService.traceLineageBackwards(datasetId);
    }

    @CrossOrigin("*")
    @GetMapping("/forwards/{datasetId}")
    public String traceForwards(@PathVariable(name = "datasetId") String datasetId) throws Exception {
        return lineageService.traceLineageForward(datasetId);
    }

    @CrossOrigin("*")
    @GetMapping("/all")
    public String getAllLineage() throws Exception {
        return lineageService.getAllLineageData();
    }
}
