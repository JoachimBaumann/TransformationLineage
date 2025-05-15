package sdu.masters;

import java.util.List;

public class LineageRecord {
    public String transformationId;
    public String transformationName;
    public String transformationVersion;
    public String transformationType;
    public String timestamp;

    public List<String> inputPaths;    // multiple allowed
    public String outputPath;
    public String datasetFormat;
}