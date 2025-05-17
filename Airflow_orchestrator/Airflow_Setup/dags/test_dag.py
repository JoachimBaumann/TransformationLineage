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
        application='/data/scripts/wordcount.py',  # Python script path inside container
        application_args=['/data/1.txt', '/data/output/1'],  # Adjust as needed
        name='pyspark_wordcount_job',
        deploy_mode='cluster',
        conf={
        },
        verbose=True
    )