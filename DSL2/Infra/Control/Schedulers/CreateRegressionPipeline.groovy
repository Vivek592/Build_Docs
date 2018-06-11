def parent_folder = 'Infra/Control/Schedulers'
def env_name = "${ENV_PREFIX}"
def schedule = "${Schedule}"
def env_job_folder = '/'+parent_folder+'/'+env_name+'/'
folder(parent_folder)
folder(parent_folder+'/'+env_name)

job(env_job_folder+"Regression"+env_name) {
  displayName("Regression Pipeline for "+env_name)
  triggers {
     cron(schedule)
  }
  publishers {
    downstreamParameterized {
      trigger('/Infra/Pipelines/RegressionTest/Pull-Code') {
        condition('SUCCESS')
        parameters{
          predefinedProp('ENV_PREFIX', env_name)
          predefinedProp('ATGSlave', "atg-${ENV_PREFIX}-aws-jsa01")
          predefinedProp('branch', "develop")
          predefinedProp('DB_SERVER', "czudcasym0md")
        }
      }
    }
  }
}
