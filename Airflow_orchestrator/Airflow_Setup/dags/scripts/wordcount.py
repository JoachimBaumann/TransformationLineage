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
    spark.conf.set("spark.custom.jobName", "WordCount")

    # ✅ Read text file and create temp view
    lines = spark.read.text(input_file)
    lines.createOrReplaceTempView("lines")

    # ✅ SQL query equivalent
    words = spark.sql("""
        SELECT explode(split(value, '\\s+')) AS word
        FROM lines
    """).filter(col("word") != "")

    # ✅ Group by word and count
    counts = words.groupBy("word").count().orderBy(desc("count"))

    # ✅ Write result to output
    counts.write.format("csv").save(output_dir)

    spark.stop()

if __name__ == "__main__":
    main()
