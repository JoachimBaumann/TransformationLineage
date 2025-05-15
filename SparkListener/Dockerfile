FROM docker.io/bitnami/spark:3.5

# Copy your custom listener jar into the image
COPY target/SparkListener-1.0-SNAPSHOT.jar /opt/bitnami/spark/listeners/

# Set Spark defaults to include the listener
ENV SPARK_EXTRA_CLASSPATH="/opt/bitnami/spark/listeners/spark-listener.jar"

# Optional: auto-register listener via Spark config
ENV SPARK_SUBMIT_OPTIONS="--conf spark.extraListeners=sdu.masters.GlobalMetadataListener"
