def under_folder = '/ATG/Database'
folder(under_folder)

job(under_folder+'/dbIncAndLoadDataFor') {
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
        stringParam('staging_URL','jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
        stringParam('production_URL','jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
	stringParam('switching_a_URL','jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
	stringParam('switching_b_URL','jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
	stringParam('publishing_URL','dbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
	stringParam('agent_URL','jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
       
    }
    deliveryPipelineConfiguration("database","updateDB");
        

    steps {
        shell('cd ${DB_UPDATE_FOLDER}/${ENV_PREFIX} \n'+
		'export ATG_HOME=/app/ecomm/ATG/ATG10.1.2 \n'+
		'export JBOSS_HOME=/tmp \n'+
		'export JAVA_HOME=/usr/java/jdk1.6.0_45 \n'+
		'export PATH=/app/ecomm/tools/jruby/bin:$JAVA_HOME/bin:$PATH \n'+
		'buildr ATG:BQ:env-install:db_inc ATG:BQ:env-install:data_load_data -e ${ENV_PREFIX} --trace=all')
    }
}