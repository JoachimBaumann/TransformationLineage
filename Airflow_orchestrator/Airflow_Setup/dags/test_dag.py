from airflow import DAG
from airflow.providers.apache.spark.operators.spark_submit import SparkSubmitOperator
from datetime import datetime

default_args = {
    'owner': 'airflow',
    'start_date': datetime(2024, 1, 1),
    'depends_on_past': False,
    'retries': 0,
}

with DAG(
    dag_id='spark_wordcount_pyspark',
    default_args=default_args,
    catchup=False,
    tags=['spark', 'example'],
) as dag:

    submit_spark_job = SparkSubmitOperator(
        task_id='run_pyspark_wordcount',
        conn_id='Spark',  # keep lowercase if that’s your connection ID
        application='/opt/airflow/dags/scripts/wordcount.py',  # Python script path inside container
        application_args=['/data/input.txt', '/data/output'],  # Adjust as needed
        name='pyspark_wordcount_job',
        conf={
            "spark.executor.memory": "1g",
            "spark.driver.memory": "1g",
            "spark.master": "spark://spark:7077"  # Points to your Spark cluster
        },
        verbose=True
    )