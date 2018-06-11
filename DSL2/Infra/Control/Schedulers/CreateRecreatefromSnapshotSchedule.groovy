def parent_folder = 'Infra/Control/Schedulers'
def env_name = "${ENV_PREFIX}"
def shared_slave = "${SHARED_SLAVE}"
def charge_code = "${ChargeCode}"
def schedule = "${Schedule}"
def env_job_folder = '/'+parent_folder+'/'+env_name+'/'
def integratedEnv = "${IntegratedEnv}"
folder(parent_folder)
folder(parent_folder+'/'+env_name)

job(env_job_folder+"Rebuild"+env_name) {
  displayName("Rebuild from Snapshot Pipeline "+env_name)
  triggers {
     cron(schedule)
  }
  publishers {
    downstreamParameterized {
      trigger('/Infra/Pipelines/EnvRecreateWithSnapshot/StopATG-Flow') {
        condition('SUCCESS')
        parameters{
          predefinedProp('ENV_PREFIX', env_name)
          predefinedProp('DeploymentSlave', "atg-${ENV_PREFIX}-aws-jsa01")
          predefinedProp('SharedSlave', shared_slave)
          predefinedProp('Charge_Code', charge_code)
          predefinedProp('IntegratedEnv', integratedEnv)
        }
      }
    }
  }
}
