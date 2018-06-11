def under_folder = '/ATG/Database/'
def job_name = under_folder +'dbIncAndLoadDataFor'

job(job_name) {
    description 'Database Increment and Load Data job'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')

        stringParam('production_jdbc_password','prod_pass','Production Schema Password')
        stringParam('switching_b_jdbc_password','switching_b_pass','switching b Schema Password')
        stringParam('switching_a_jdbc_password','switching_a_pass','switching a Schema Password')
        stringParam('publishing_jdbc_password','publishing_pass','publishing Schema Password')
        stringParam('staging_jdbc_password','stag_pass','staging Schema Password')
        stringParam('agent_jdbc_password','agent_pass','Production Schema Password')

       
    }
    
        deliveryPipelineConfiguration("database","updateDB");

    steps {
    
       shell('cd ${DB_UPDATE_FOLDER}/qam \n buildr db_inc data_load_data -e qam --trace')
    }
    
    }