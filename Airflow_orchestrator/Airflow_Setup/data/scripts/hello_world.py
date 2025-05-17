from pyspark.sql import SparkSession

if __name__ == "__main__":
    spark = SparkSession.builder.appName("HelloWorldDF").getOrCreate()

    df = spark.createDataFrame([("Hello, world!",)], ["message"])
    rows = df.collect()

    print("Collected rows:", rows)

    output_path = "/data/output/hello_df_driver_write.csv"
    try:
        with open(output_path, "w") as f:
            f.write("message\n")
            for row in rows:
                print("Writing row:", row)
                f.write(f"{row['message']}\n")
        print(f"Wrote CSV from driver to: {output_path}")
    except Exception as e:
        print("Exception during write:", e)

    spark.stop()
