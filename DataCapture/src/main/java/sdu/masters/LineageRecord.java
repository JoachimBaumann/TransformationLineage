package sdu.masters;

import java.util.List;

public class LineageRecord {
    public String jobName;
    public String transformationId;
    public String transformationName;
    public String timestamp;
    public long duration;
    public List<String> inputPaths;    // multiple allowed
    public String outputPath;

}