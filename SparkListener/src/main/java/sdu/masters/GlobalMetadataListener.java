package sdu.masters;
import org.apache.spark.scheduler.*;
import scala.collection.JavaConverters;

public class GlobalMetadataListener extends SparkListener {

    @Override
    public void onJobStart(SparkListenerJobStart jobStart) {
        System.out.println("[SparkListener] Job started: " + jobStart.jobId());
    }

    @Override
    public void onStageCompleted(SparkListenerStageCompleted stageCompleted) {
        StageInfo stageInfo = stageCompleted.stageInfo();
        System.out.println("[SparkListener] Stage " + stageInfo.stageId() + " completed.");

        JavaConverters.seqAsJavaList(stageInfo.rddInfos()).forEach(rdd -> {
            String rddName = rdd.name();
            if (rddName != null && rddName.contains("text")) {
                System.out.println("[Input] RDD: " + rddName);
            }
        });
    }


    @Override
    public void onApplicationEnd(SparkListenerApplicationEnd appEnd) {
        System.out.println("[SparkListener] Application ended at: " + appEnd.time());
    }
}
