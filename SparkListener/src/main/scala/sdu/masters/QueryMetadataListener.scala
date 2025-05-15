package sdu.masters

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.execution.FileSourceScanExec

class QueryMetadataListener extends QueryExecutionListener {

  private val producer = new KafkaLineageProducer("kafka:9092", "LineageEvent")

  override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
    val spark = SparkSession.active
    val jobName = spark.conf.get("spark.custom.jobName", "UnknownJob")
    val durationMs = durationNs / 1000000

    val inputPaths = qe.sparkPlan.collect {
      case fs: FileSourceScanExec => fs.relation.location.rootPaths.map(_.toString)
    }.flatten.distinct.mkString(",")

    val outputPath = qe.logical match {
      case insert: InsertIntoHadoopFsRelationCommand => insert.outputPath.toString
      case _ => "N/A"
    }

    // Skip events that are not "real" jobs with meaningful output
    if (inputPaths.nonEmpty && outputPath != "N/A") {
      val lineageJson =
        s"""{
           |  "jobName": "$jobName",
           |  "inputPaths": "$inputPaths",
           |  "outputPath": "$outputPath",
           |  "durationMs": $durationMs,
           |  "status": "SUCCESS"
           |}""".stripMargin

      println(s"[METADATA] job=$jobName input=$inputPaths output=$outputPath duration=${durationMs}ms")
      producer.sendEvent(lineageJson)
    } else {
      println(s"[SKIPPED] job=$jobName — incomplete or non-output operation")
    }
  }

  override def onFailure(funcName: String, qe: QueryExecution, exception: Exception): Unit = {
    val spark = SparkSession.active
    val jobName = spark.conf.get("spark.custom.jobName", "UnknownJob")
    println(s"[METADATA] job=$jobName status=FAILED reason=${exception.getMessage}")
  }
}
