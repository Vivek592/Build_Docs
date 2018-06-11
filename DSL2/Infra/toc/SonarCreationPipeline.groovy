
def under_folder = 'Infra/Pipelines/SonarCreatePipelines'
folder(under_folder)
{
  primaryView('Sonar-Creation-Pipeline')
}

def initial_job= 'CreateSonar'
rds_user= 'sonar'
rds_passwd= 'ChangeMeNow'

buildFlowJob(under_folder+"/CreateSonar") {
     displayName("Create Team Sonar")
 
    parameters {
        stringParam('team','','Team Name')
        stringParam('Charge_Code','','Team Charge Code')
        stringParam('SonarDBName','','Team Name')
        labelParam('SharedSlave')
        {
        defaultValue('atg-toc-aws-slv01')
        }
             
    }  
   buildFlow(
        "build('/Infra/toc/CreateSonar', team: params['team'],Charge_Code: params['Charge_Code'], SharedSlave: params['SharedSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
       publishers {
          downstreamParameterized {
                trigger('CreateSonarDB') {
                      condition('UNSTABLE_OR_BETTER')  
                      parameters{                
                      currentBuild() 
                      }                    
                }
            }
                
      }          
 }
 
 
 
buildFlowJob(under_folder+"/CreateSonarDB") {
     displayName("Create Sonar DB")
 
    parameters {
        stringParam('team','','Team Name')
        stringParam('SonarDBName','','Team Name')
        
        labelParam('SharedSlave')
        {
        defaultValue('atg-toc-aws-slv01')
        }
             
    }  
 
  configure { project -> project / 'buildNeedsWorkspace'('true') }
  
         buildFlow(
      	  "build('/Infra/toc/CreateMySQLdb', team: params['team'],Charge_Code: params['Charge_Code'], SharedSlave: params['SharedSlave'],rds_db_name: params['SonarDBName'], rds_user: '"+rds_user+"', rds_passwd:'"+rds_passwd+"')"
       )
       publishers {
          downstreamParameterized {
                trigger('ConfigureSonar') {
                      condition('UNSTABLE_OR_BETTER')  
                      parameters{                
                      currentBuild() 
                      }                    
                }
            }
                
      }       
                
 }
 
 
 buildFlowJob(under_folder+"/ConfigureSonar") {
     displayName("Configure Sonar ")
 
    parameters {
        stringParam('team','','Team Name')
        stringParam('SonarDBName','','Team Name')
        
        labelParam('SharedSlave')
        {
        defaultValue('atg-toc-aws-slv01')
        }
             
    }  
     configure { project -> project / 'buildNeedsWorkspace'('true') }
     
     buildFlow(
      	  "build('/Infra/toc/ConfigureSonar', team: params['team'],Charge_Code: params['Charge_Code'], SharedSlave: params['SharedSlave'],rds_db_name: params['SonarDBName'], rds_user: '"+rds_user+"', rds_passwd:'"+rds_passwd+"')"
       )   
           
           
           publishers {
          downstreamParameterized {
                trigger('CreateSonarStrategy') {
                      condition('UNSTABLE_OR_BETTER')  
                      parameters{                
                      currentBuild() 
                      }                    
                }
            }
                
      }           
 }
 
 
  
job(under_folder+"/CreateSonarStrategy") {
     displayName("Create Strategy")
 
    parameters {
        stringParam('team','','Team Name')
        labelParam('SharedSlave')
        {
        defaultValue('atg-toc-aws-slv01')
        }
             
    }  
   steps {
        dsl {
            text(readFileFromWorkspace('Infra/toc/SonarStrategy.include'))
        }
    }    
                
 }
 
     
deliveryPipelineView(under_folder+'/Sonar-Creation-Pipeline') {
    enableManualTriggers true
    pipelineInstances 5
    configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    
    pipelines {
        component('Sonar Creation Pipeline', initial_job)
    }
    }
