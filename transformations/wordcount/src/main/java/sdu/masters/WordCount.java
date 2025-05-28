package sdu.masters;

import org.apache.spark.sql.*;
import org.apache.spark.api.java.function.FilterFunction;


public class WordCount {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Usage: WordCount <input_file> <output_directory>");
            System.exit(1);
        }

        SparkSession spark = SparkSession.builder()
                .appName("WordCount")
                .getOrCreate();
        spark.conf().set("spark.custom.jobName", "wordcount");
        //spark.sparkContext().addSparkListener(new GlobalMetadataListener());
        //spark.listenerManager().register(new QueryMetadataListener());

        // ✅ Read as DataFrame and create temp view
        Dataset<String> lines = spark.read().textFile(args[0]);
        lines.createOrReplaceTempView("lines");

        // ✅ SQL that triggers query execution
        Dataset<Row> words = spark.sql("SELECT explode(split(value, '\\\\s+')) AS word FROM lines")
                .filter((FilterFunction<Row>) row -> !row.getString(0).isEmpty());

        Dataset<Row> counts = words.groupBy("word").count().orderBy(functions.desc("count"));

        counts.write().format("txt").save(args[1]);

        spark.stop();
    }
}
