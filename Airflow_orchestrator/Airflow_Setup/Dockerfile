FROM apache/airflow:3.0.1

USER root

# Install core utils
RUN apt-get update && \
    apt-get install -y --no-install-recommends \
        curl \
        wget \
        gnupg \
        procps \
        ca-certificates && \
    rm -rf /var/lib/apt/lists/*

# Install OpenJDK 11 from Adoptium (Temurin)
RUN mkdir -p /opt/java && \
    curl -L https://github.com/adoptium/temurin11-binaries/releases/download/jdk-11.0.22+7/OpenJDK11U-jdk_x64_linux_hotspot_11.0.22_7.tar.gz | tar -xz -C /opt/java && \
    mv /opt/java/jdk-11.0.22+7 /opt/java/openjdk-11

ENV JAVA_HOME=/opt/java/openjdk-11
ENV PATH=$JAVA_HOME/bin:$PATH

# Install Spark 3.4.1
RUN curl -L https://archive.apache.org/dist/spark/spark-3.4.1/spark-3.4.1-bin-hadoop3.tgz | tar -xz -C /opt/ && \
    mv /opt/spark-3.4.1-bin-hadoop3 /opt/spark

ENV SPARK_HOME=/opt/spark
ENV PATH=$SPARK_HOME/bin:$PATH

USER airflow

# Install the Apache Spark provider for Airflow
RUN pip install apache-airflow-providers-apache-spark