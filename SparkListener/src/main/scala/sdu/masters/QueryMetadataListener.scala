package sdu.masters

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.execution.FileSourceScanExec

import java.util.UUID
import java.time.Instant

class QueryMetadataListener extends QueryExecutionListener {

  private val producer = new KafkaLineageProducer("joachimbaumann.dk:9092", "LineageEvent")

  override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
    val spark = SparkSession.active
    val jobName = spark.conf.get("spark.custom.jobName", "UnknownJob")
    val durationMs = durationNs / 1000000


    val inputPathsList = qe.sparkPlan.collect {
      case fs: FileSourceScanExec => fs.relation.location.rootPaths.map(_.toString)
    }.flatten.distinct

    val inputPathsJsonArray = inputPathsList.map(p => s""""$p"""").mkString("[", ", ", "]")


    val outputPath = qe.logical match {
      case insert: InsertIntoHadoopFsRelationCommand => insert.outputPath.toString
      case _ => "N/A"
    }

    if (inputPathsList.nonEmpty && outputPath != "N/A") {
      val transformationId = UUID.randomUUID().toString
      val transformationName = jobName
      val timestamp = Instant.now().toString

      val inputPathsJsonArray = inputPathsList.map(p => s""""$p"""").mkString("[", ", ", "]")

      val lineageJson =
        s"""{
           |  "transformationId": "$transformationId",
           |  "transformationName": "$transformationName",
           |  "timestamp": "$timestamp",
           |  "duration": $durationMs,
           |  "inputPaths": $inputPathsJsonArray,
           |  "outputPath": "$outputPath"
           |}""".stripMargin

      println(s"[LINEAGE] $lineageJson")
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
