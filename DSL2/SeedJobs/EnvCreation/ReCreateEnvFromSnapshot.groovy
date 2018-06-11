def parent_folder = '/Infra/Pipelines'
def folder_name = "${PIPELINE_FOLDER_NAME}"
def under_folder = '/'+parent_folder+'/'+folder_name+'/'
def initial_job = "StopATG-Flow"
def ansible_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def atg_slave = 'atg-${ENV_PREFIX}-aws-jsa01'
def shared_slave = "${SHARED_SLAVE}"
def infra_code_branch= "${INFRA_CODE_BRANCH}"
def eac_release= "${EAC_RELEASE}"
def ss_release= "${SS_RELEASE}"
def dryRun="${DRY_RUN}"
def wmb_mock_release = "1"

folder(parent_folder)
folder(parent_folder+'/'+folder_name)
{
  primaryView('Environment-ReCreate-Pipeline')
}

buildFlowJob(under_folder+"StopATG-Flow") {
     displayName("Stop  ATG")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')
        stringParam('SharedSlave',shared_slave,'Environment  pre-fix')
        stringParam('Charge_Code','','Charge Code for env')
        stringParam('IntegratedEnv','no','Is this an Integrated Env')
        stringParam('SiloCount','1','Required number of instances for SiloEnv')
        stringParam('branch',infra_code_branch,'Infrastructure Code Branch')
        choiceParam('EndecaAppName',['bqendeca','caendecafrfr','bdendecafrfr'],'Endeca Application to be deployed (e.g. "caendecafrfr" for Casto, "bqendeca" for DIY)')
		choiceParam('OPCO',['BQUK','CAFR','BDFR'])
    }
   buildFlow(
        dryRun+"build('Infra/Build/CheckoutInfrCode', DeploymentSlave: params['SharedSlave'], branch: params['branch']) \n"+
        dryRun+"build('Infra/Build/CheckoutInfrCode', DeploymentSlave: params['DeploymentSlave'], branch: params['branch']) \n"+
        dryRun+"build('ATG/Deploy/RestartAllATGApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['DeploymentSlave'], action: 'stop')"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('Backup-Build-Flow,Backup-DB-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()
                      }
                }
            }

            joinTrigger {
            publishers {
                downstreamParameterized {
                        trigger('Delete-Instances-Flow') {
                              condition('UNSTABLE_OR_BETTER')
                              parameters{
                          predefinedProp('ATGSlave',atg_slave)
                              currentBuild()
                              }
                              }
                          }
                }
        }

    }
    }

buildFlowJob(under_folder+"Backup-DB-Flow") {
     displayName("Backup DB")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('DeploymentSlave')


    }
   buildFlow(
        dryRun+"build('/Infra/Database/CreateSnapshotAndDeleteRDS',snapshot_name: params['ENV_PREFIX']+'-snapshot-RC' , DeploymentSlave: params['DeploymentSlave'], ENV_PREFIX: params['ENV_PREFIX'])"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }


 }

buildFlowJob(under_folder+"Backup-Build-Flow") {
     displayName("Backup  Builds")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('SharedSlave')


    }
   buildFlow(
        dryRun+"build('ATG/Release/CopyBuildFromEnvSlaveToSharedSlave' , SharedSlave: params['SharedSlave'], ENV_PREFIX: params['ENV_PREFIX'], DeploymentSlave: params['DeploymentSlave'])"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }


    }

 buildFlowJob(under_folder+"Delete-Instances-Flow") {
     displayName("Delete ATG Environment and Akana APIs")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam("SharedSlave")
    }

   buildFlow(
       dryRun+ "build('Infra/Delete/DeleteATGEnvAndAkanaAPIs' , DeploymentSlave: params['DeploymentSlave'],SharedSlave: params['SharedSlave'], ENV_PREFIX: params['ENV_PREFIX'], IntegratedEnv: params['IntegratedEnv'])"
    )

    configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('Recreate-Cloud-Environment-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()
                      }
                }
            }

    }
    }


buildFlowJob(under_folder+"Recreate-Cloud-Environment-Flow") {
    displayName("Recreate Environment")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('SharedSlave',shared_slave,'Initial slave to launch')
        stringParam('AnsibleSlave',ansible_slave,'Initial slave to launch')
        stringParam('Charge_Code','','Charge code')
        stringParam('branch',infra_code_branch,'Infrastructure Code Branch')
    }
   label(shared_slave)

   buildFlow(
          dryRun+" build('Infra/Deploy/CreateATGSlave',ENV_PREFIX: params['ENV_PREFIX'] , GenericSlave: params['SharedSlave'], Charge_Code: params['Charge_Code'] ) \n"+
          dryRun+" build('Infra/Build/CheckoutInfrCode',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01', branch: params['branch']) \n"+
          dryRun+" build('Infra/Deploy/PrepareS3File' ,ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: 'atg-'+params['ENV_PREFIX']+'-aws-jsa01')"
       )
   configure { project -> project / 'buildNeedsWorkspace'('true') }
    publishers {
          downstreamParameterized {
                trigger('CreateMQGateWay-Flow,Create-ATGEstate-Flow,Create-Endeca-Flow,Create-ATGSearch-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()
                      predefinedProp('AnsibleSlave',ansible_slave)
                      predefinedProp('ATGSlave',atg_slave)
                      }

                }

            }

    }

    }



buildFlowJob(under_folder+"Create-ATGEstate-Flow") {
     displayName("Create ATG Estate")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(

       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }


    publishers {
          downstreamParameterized {
                trigger('Restore-RDS-Snapshot-Flow,Create-ATGEnv-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()
                      predefinedProp('AnsibleSlave',ansible_slave)
                      predefinedProp('ATGSlave',atg_slave)
                      }

                }


              joinTrigger {
            publishers {
                downstreamParameterized {
                        trigger('DeployATG-Flow,DeployWMBMocks-Flow') {
                              condition('SUCCESS')
                              parameters{
                              predefinedProp('AnsibleSlave',ansible_slave)
                          predefinedProp('ATGSlave',atg_slave)
                              currentBuild()
                              }
                              }
                          }
                }
            }

            }
        }

    }


buildFlowJob(under_folder+"Create-ATGSlave-Flow") {
     displayName("Create-ATG-Slave")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
        dryRun+"build('Infra/Deploy/CreateATGSlave',ENV_PREFIX: params['ENV_PREFIX'] , SharedSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'] ) \n"+
        dryRun+"build('Infra/Deploy/manageAwsSecGroupRules',ENV_PREFIX: params['ENV_PREFIX'] , SharedSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'], integratedEnv: params['IntegratedEnv'] ) \n"+
       dryRun+" build('ATG/Update/UpdateATGSlave',ENV_PREFIX: params['ENV_PREFIX'] , ATGSlave: params['ATGSlave'] ) "
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }



    }

buildFlowJob(under_folder+"Create-ATGEnv-Flow") {
     displayName("Create ATGEnv")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "build('Infra/Deploy/CreateAllATGAppEnvOpco',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'],Charge_Code: params['Charge_Code'], IntegratedEnv: params['IntegratedEnv'], SiloCount: params['SiloCount'],OPCO: params['OPCO'])"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }

    }



 buildFlowJob(under_folder+"Create-ATGSearch-Flow") {
 displayName("Create ATG Search")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "build('Infra/Deploy/CreateAllATGSearch',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'])"
       )
    configure { project -> project / 'buildNeedsWorkspace'('true') }

    }



buildFlowJob(under_folder+"Restore-RDS-Snapshot-Flow") {
    displayName("Restore RDS")
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
      dryRun+  "build('Infra/Database/UpdateSnapshotNameInProperties', DeploymentSlave: params['AnsibleSlave'], snapshot_name: params['ENV_PREFIX']+'-snapshot-RC')\n"+
       dryRun+"  build('Infra/Database/RestoreRDSAndDeleteSnapshot',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )
    configure { project -> project / 'buildNeedsWorkspace'('true') }


    }

buildFlowJob(under_folder+"CreateMQGateWay-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "build('WMB/Deploy/CreateMQGateway',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'])"
       )
   configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('TestMQGateway-Flow') {
                    condition('UNSTABLE_OR_BETTER')
                    parameters{
                    currentBuild()
                    }
                }
            }
        }

    }



buildFlowJob(under_folder+"TestMQGateway-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
        dryRun+"build('WMB/Test/TestMQGateWay',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )
    configure { project -> project / 'buildNeedsWorkspace'('true') }




    }



buildFlowJob(under_folder+"DeployATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('SharedSlave',shared_slave,'Initial slave to launch')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "build('ATG/Release/CopyBuildFromSharedSlaveToEnvSlave' ,ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['AnsibleSlave'],SharedSlave: params['SharedSlave'] )\n"+
       dryRun+ "build('ATG/Build/BQ/Release/CopyEarsToAnsibleDir' ,ENV_PREFIX: params['ENV_PREFIX'],  AnsibleSlave: params['AnsibleSlave'])\n"+
       dryRun+" build('ATG/Deploy/CreateATGLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'],  DeploymentSlave: params['AnsibleSlave'])\n"+
       dryRun+" build('ATG/Deploy/DeployAllATGApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave']) \n"+
       dryRun+" build('ATG/Deploy/DeployAllLocalConfigs',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )
   configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('StartATG-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      currentBuild()
                      }
                }
            }
        }

    }

buildFlowJob(under_folder+"StartATG-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')


    }
   buildFlow(
        dryRun+"build('ATG/Deploy/StartATGEnv',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('Endeca-Indexing-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                      predefinedProp('ATGSlave',atg_slave)
                      currentBuild()
                      }
                }
            }
        }

    }


buildFlowJob(under_folder+"Endeca-Indexing-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "println 'Sleep for 10 Minutes - give systems time to settle'\n"+
	   dryRun+ "sleep(600000)\n"+
       dryRun+ "build('ATG/Deploy/Endeca/TriggerBaselineIndex',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }

	publishers {
        downstreamParameterized {
            trigger('Bulk-Indexing-Flow') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                        currentBuild()
                      }
                    }
            }
        }
    }
	
buildFlowJob(under_folder+"Bulk-Indexing-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "println 'Sleep for 10 Minutes - give systems time to settle'\n"+
	   dryRun+ "sleep(600000)\n"+
       dryRun+ "build('ATG/Deploy/Endeca/TriggerBulkIndex',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'])"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }

	publishers {
        downstreamParameterized {
            trigger('Test-EndPoints') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                        currentBuild()
                      }
                    }
            }
        }
    }

buildFlowJob(under_folder+"Test-EndPoints") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
    }
   buildFlow(
       dryRun+ "println 'Sleep for 10 Minutes - give systems time to settle'\n"+
       dryRun+ "sleep(600000)\n"+
       dryRun+ "build('Infra/Deploy/TestAllATGEndPoints',ENV_PREFIX: params['ENV_PREFIX'], DeploymentSlave: params['DeploymentSlave'], integratedEnv: params['IntegratedEnv'])"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }
        publishers {
        downstreamParameterized {
            trigger('TestCISuite') {
                      condition('UNSTABLE_OR_BETTER')
                      parameters{
                        currentBuild()
                      }
                    }
            }
        }

    }

buildFlowJob(under_folder+"TestCISuite") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('RunWhere','DEFAULTGRID','')
        stringParam('Browser','chrome','')
        labelParam('TestSlaveLabel') {
          defaultValue('ACTIVATE_TESTER')
        }
        
    }
   buildFlow(
       dryRun+ "build('ATG/Test/ExtractTestMaterialForAllTestSlaves',branch: 'master_cr4')\n"+
	   dryRun+ "build('AutomatedTests/DynamicArea/HighSuite/RunBQCISuite',ENV_PREFIX: params['ENV_PREFIX'] , TestSlaveLabel: params['TestSlaveLabel'], RunWhere: params['RunWhere'], Browser: params['Browser'])\n"
       /*dryRun+ "build('AutomatedTests/DynamicArea/HighSuite/CheckoutWithDsvAndSfcProduct',ENV_PREFIX: params['ENV_PREFIX'] , TestSlaveLabel: params['TestSlaveLabel'], RunWhere: params['RunWhere'], Browser: params['Browser'], GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',testName: 'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.CheckoutWithDsvAndSfcProduct',site: 'DIY')\n"+
       dryRun+ "build('AutomatedTests/DynamicArea/HighSuite/Esp13ProductSearchResult',ENV_PREFIX: params['ENV_PREFIX'] , TestSlaveLabel: params['TestSlaveLabel'], RunWhere: params['RunWhere'], Browser: params['Browser'], GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',testName: 'EndecaTests.IntegrationTestSuite.Esp13ProductSearchResult',site: 'DIY')\n"+
       dryRun+ "build('AutomatedTests/DynamicArea/HighSuite/SearchProduct',ENV_PREFIX: params['ENV_PREFIX'] , TestSlaveLabel: params['TestSlaveLabel'], RunWhere: params['RunWhere'], Browser: params['Browser'], GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',testName: 'DarwinAcceptanceTests.WebTpSuiteForSqa3.Sqa3Scripts.SearchProduct',site: 'TP')\n"+
       dryRun+ "build('AutomatedTests/DynamicArea/HighSuite/CheckoutWithDsvProduct',ENV_PREFIX: params['ENV_PREFIX'] , TestSlaveLabel: params['TestSlaveLabel'], RunWhere: params['RunWhere'], Browser: params['Browser'], GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',testName: 'DarwinAcceptanceTests.IntegrationWebForAgent.TestForCloud.CheckoutWithDsvProduct',site: 'AGENT')\n"+
       dryRun+ "build('AutomatedTests/DynamicArea/HighSuite/BccDeployment',ENV_PREFIX: params['ENV_PREFIX'] , TestSlaveLabel: params['TestSlaveLabel'], RunWhere: params['RunWhere'], Browser: params['Browser'], GridURL: 'http://atg-shared-aws-selenium01.aws.gha.kfplc.com:4445/wd/hub',testName: 'DarwinAcceptanceTests.IntegrationWeb.TestForCloud.BccDeployment',site: 'DIY')"*/
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }


    }



 buildFlowJob(under_folder+"Create-Endeca-Flow") {
     displayName("Create-Endeca-Servers")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "build('Infra/Deploy/Endeca/CreateEndecaSilo',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], Charge_Code: params['Charge_Code'], EndecaAppName: params['EndecaAppName'] ) "
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }


    publishers {
          downstreamParameterized {
                trigger('Deploy-Endeca-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
                currentBuild()
                }
                }
            }
        }

    }


  buildFlowJob(under_folder+"Deploy-Endeca-Flow") {
     displayName("Deploy-Endeca-Servers")

    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('AnsibleSlave')

    }
   buildFlow(
       dryRun+ "build('ATG/Deploy/Endeca/DeployEndecaApps',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['AnsibleSlave'], EAC_RELEASE: '"+eac_release+"', SS_RELEASE: '"+ss_release+"' , deploy_mode: 'init',APP_NAME: params['EndecaAppName'] )"
       )

    configure { project -> project / 'buildNeedsWorkspace'('true') }

    publishers {
          downstreamParameterized {
                trigger('Test-Endeca-Flow') {
                condition('UNSTABLE_OR_BETTER')
                parameters{
                currentBuild()
                }
                }
            }
        }

    }

  buildFlowJob(under_folder+"DeployWMBMocks-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        stringParam('APP_VERSION',wmb_mock_release,'ATG Application Release ')
        labelParam('ATGSlave')
    }

   buildFlow(
        dryRun+"build('WMB/Deploy/DeployAllWMBMocks',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], APP_VERSION: params['APP_VERSION'])"
       )
   configure { project -> project / 'buildNeedsWorkspace'('true') }

           publishers {
          downstreamParameterized {
                trigger('StartWMBMocks-Flow') {
                    condition('UNSTABLE_OR_BETTER')
                    parameters{
                    currentBuild()
                    }
                }
            }
        }
   }

   buildFlowJob(under_folder+"StartWMBMocks-Flow") {
    parameters {
        stringParam('ENV_PREFIX','','Environment  pre-fix')
        labelParam('ATGSlave')

    }
   buildFlow(
        dryRun+"build('WMB/Deploy/StopStartTomcat',ENV_PREFIX: params['ENV_PREFIX'] , DeploymentSlave: params['ATGSlave'], action:'restart')"
       )
   configure { project -> project / 'buildNeedsWorkspace'('true') }
    }

deliveryPipelineView(under_folder+'Environment-ReCreate-Pipeline') {
    showAggregatedPipeline()
    enableManualTriggers true
    pipelineInstances 5
    configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }

    pipelines {
        component('Environment Recreation Pipeline', initial_job)
    }
    }
