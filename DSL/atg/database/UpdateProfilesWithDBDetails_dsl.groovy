def under_folder = '/ATG/Database'
folder(under_folder)

job(under_folder +'/UpdateProfilesWithDBDetails') {
    description 'Database Increment and Load Data job'
    logRotator {
      daysToKeep -1
      numToKeep 30
    }
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('DB_STRING','${ENV_PREFIX}: \n'+
        '<<: *kits_common_test_env settings: \n'+ 
        '<<: *kits_common_test_env_settings \n'+
        'env_prefix: ${ENV_PREFIX} \n'+
        'oracle_host: ${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com \n'+
        'jdbc_url_sid_separator: / \n'+
        'oracle_sid: ${ENV_PREFIX} \n'+
        'oracle_non_pub_sid: ${ENV_PREFIX} \n'+
        'oracle_port: 1527 \n'+
        'staging_URL: jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX}))) \n'+
        'production_URL: jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX}))) \n'+
        'switching_a_URL: jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX}))) \n'+
        'switching_b_URL: jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX}))) \n'+
        'publishing_URL: jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=${ENV_PREFIX}.cgedocbjqkfs.eu-west-1.rds.amazonaws.com)(PORT=1527))(CONNECT_DATA =(SID=${ENV_PREFIX})))')
	labelParam('DeploymentSlave')       
    }
    deliveryPipelineConfiguration("database","updateDB");
        

    steps {
        shell('echo ${DB_STRING} >> profiles.txt')
    }
}