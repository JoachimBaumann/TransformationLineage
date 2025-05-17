from pyspark.sql import SparkSession

if __name__ == "__main__":
    spark = SparkSession.builder.appName("HelloWorldDF").getOrCreate()

    df = spark.createDataFrame([("hello",), ("world",)], ["word"])

    rows = df.collect()  # Trigger job

    with open("/data/output/driver_hello.csv", "w") as f:
        f.write("word\n")
        for row in rows:
            f.write(f"{row['word']}\n")

    print("Driver wrote CSV to /data/output/driver_hello.csv")
    spark.stop()
