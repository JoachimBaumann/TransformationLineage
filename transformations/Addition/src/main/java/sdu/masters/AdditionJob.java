package sdu.masters;

import org.apache.spark.sql.*;

public class AdditionJob {
    public static void main(String[] args) {
        SparkSession spark = SparkSession.builder().appName("Addition").getOrCreate();
        spark.conf().set("spark.custom.jobName", "Addition");

        // Read single-value .txt files from GCS
        Dataset<Row> input1 = spark.read()
                .text(args[0])
                .withColumnRenamed("value", "value1");

        Dataset<Row> input2 = spark.read()
                .text(args[1])
                .withColumnRenamed("value", "value2");

        input1.createOrReplaceTempView("input1");
        input2.createOrReplaceTempView("input2");

        Dataset<Row> result = spark.sql(
                "SELECT CAST(a.value1 AS DOUBLE) + CAST(b.value2 AS DOUBLE) AS result " +
                        "FROM input1 a CROSS JOIN input2 b"
        );

        // Save only the result number as a .txt file (e.g., part-00000)
        result.selectExpr("CAST(result AS STRING)")
                .coalesce(1) // optional: force single output file
                .write()
                .mode(SaveMode.Overwrite)
                .text(args[2]);


        spark.stop();
    }
}
