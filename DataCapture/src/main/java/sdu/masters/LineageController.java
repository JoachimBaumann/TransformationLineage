package sdu.masters;

import org.springframework.web.bind.annotation.*;

@CrossOrigin(origins = "http://joachimbaumann.dk:3000")
@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    private final Neo4jLineageService lineageService;

    public LineageController(Neo4jLineageService lineageService) {
        this.lineageService = lineageService;
    }



    @GetMapping("/backwards/{datasetId}")
    public String traceBackwards(@PathVariable(name = "datasetId") String datasetId) throws Exception {
        return lineageService.traceLineageBackwards(datasetId);
    }


    @GetMapping("/forwards/{datasetId}")
    public String traceForwards(@PathVariable(name = "datasetId") String datasetId) throws Exception {
        return lineageService.traceLineageForward(datasetId);
    }


    @GetMapping("/all")
    public String getAllLineage() throws Exception {
        return lineageService.getAllLineageData();
    }
}
