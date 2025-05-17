from pyspark.sql import SparkSession
from pyspark.sql.functions import explode, split, col, desc
import sys

def main():
    if len(sys.argv) < 3:
        print("Usage: wordcount.py <input_file> <output_directory>", file=sys.stderr)
        sys.exit(1)

    input_file = sys.argv[1]
    output_dir = sys.argv[2]

    spark = SparkSession.builder.appName("WordCount").getOrCreate()
    spark.conf.set("spark.custom.jobName", "WordCount")  # ✅ Custom job name picked up by listener

    # ✅ Use SQL-friendly DataFrame API for lineage capture
    lines = spark.read.text(input_file)
    words_df = lines.select(explode(split(col("value"), r"\s+")).alias("word")).filter(col("word") != "")

    # ✅ This triggers lineage listener (count, groupBy, write)
    counts_df = words_df.groupBy("word").count().orderBy(desc("count"))

    # ✅ Write using insertInto-compatible method (required for outputPath detection in listener)
    counts_df.write.mode("overwrite").format("csv").save(output_dir)

    spark.stop()

if __name__ == "__main__":
    main()
