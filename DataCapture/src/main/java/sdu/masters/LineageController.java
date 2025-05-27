package sdu.masters;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/lineage")
public class LineageController {

    private final Neo4jLineageService lineageService;

    public LineageController(Neo4jLineageService lineageService) {
        this.lineageService = lineageService;
    }



    @GetMapping("/forwards")
    public String traceForwards(@RequestParam("datasetId") String datasetId) throws Exception {
        return lineageService.traceLineageForward(datasetId);
    }

    @GetMapping("/backwards")
    public String traceBackwards(@RequestParam("datasetId") String datasetId) throws Exception {
        return lineageService.traceLineageBackwards(datasetId);
    }


    @GetMapping("/all")
    public String getAllLineage() throws Exception {
        return lineageService.getAllLineageData();
    }
}
