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
        dag_id='spark_hello_world',
        default_args=default_args,
        catchup=False,
        tags=['spark', 'test'],
) as dag:

    submit_hello_job = SparkSubmitOperator(
        task_id='run_hello_spark_job',
        conn_id='Spark',  # must match connection ID in Airflow
        application='/data/scripts/hello_world.py',
        deploy_mode='client',
        name='hello_spark_job',
        verbose=True
    )
