package sdu.masters

import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.execution.QueryExecution
import org.apache.spark.sql.util.QueryExecutionListener
import org.apache.spark.sql.execution.datasources._
import org.apache.spark.sql.execution.FileSourceScanExec
import com.google.cloud.storage.{BlobId, StorageOptions}

import java.nio.channels.Channels
import java.io.{BufferedReader, InputStreamReader}
import java.util.UUID
import java.time.Instant

import org.everit.json.schema.Schema
import org.everit.json.schema.loader.SchemaLoader
import org.json.JSONObject

class QueryMetadataListener extends QueryExecutionListener {

  private val producer = new KafkaLineageProducer("joachimbaumann.dk:9092", "LineageEvent")

  private val schemaJson: String =
    """
      |{
      |  "type": "object",
      |  "required": ["transformationId", "transformationName", "timestamp", "duration", "inputPaths", "outputPath", "gitSha"],
      |  "properties": {
      |    "transformationId": {"type": "string"},
      |    "transformationName": {"type": "string"},
      |    "timestamp": {"type": "string"},
      |    "duration": {"type": "number"},
      |    "inputPaths": {"type": "array", "items": {"type": "string"}},
      |    "outputPath": {"type": "string"},
      |    "gitSha": {"type": "string"}
      |  }
      |}
    """.stripMargin

  private val schema: Schema = SchemaLoader.load(new JSONObject(schemaJson))

  override def onSuccess(funcName: String, qe: QueryExecution, durationNs: Long): Unit = {
    val spark = SparkSession.active
    val jobName = spark.conf.get("spark.custom.jobName", "UnknownJob")

    val bucket = "sparksbucket" // change this to your actual bucket
    val shaFilename = s"transformations/packagedTransformations/$jobName.txt"

    val storage = StorageOptions.getDefaultInstance.getService
    val blob = storage.get(BlobId.of(bucket, shaFilename))

    val gitSha = if (blob != null) {
      val reader = new BufferedReader(new InputStreamReader(Channels.newInputStream(blob.reader())))
      val line = reader.readLine()
      reader.close()
      if (line != null && line.startsWith("git.commit.id=")) line.split("=")(1).trim else "unknown"
    } else {
      "unknown"
    }

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

      val lineageJson =
        s"""{
           |  "transformationId": "$transformationId",
           |  "transformationName": "$transformationName",
           |  "timestamp": "$timestamp",
           |  "duration": $durationMs,
           |  "inputPaths": $inputPathsJsonArray,
           |  "outputPath": "$outputPath",
           |  "gitSha": "$gitSha"
           |}""".stripMargin

      try {
        val jsonObject = new JSONObject(lineageJson)
        schema.validate(jsonObject) // Validate against JSON Schema
        println(s"[LINEAGE] $lineageJson")
        producer.sendEvent(lineageJson)
      } catch {
        case e: Exception =>
          println(s"[VALIDATION FAILED] Schema error: ${e.getMessage}")
      }
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
