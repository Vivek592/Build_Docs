
def under_folder = 'Infra/Pipelines/JenkinsCreatePipelines'
folder(under_folder)
{
  primaryView('Jenkins-Creation-Pipeline')
}

def initial_job= 'CreateTeamHostFile'

buildFlowJob(under_folder+"/CreateTeamHostFile") {
     displayName("Create Team Host File")
 
    parameters {
        stringParam('team','','Team Name')
        stringParam('Charge_Code','','Team Charge Code')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
        
       
    }  
   buildFlow(
        "build('/Infra/toc/CreateTeamHostFile', team: params['team'], SharedSlave: params['SharedSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
         
      publishers {
          downstreamParameterized {
                trigger('CreateJenkinsMaster') {
                      condition('UNSTABLE_OR_BETTER')  
                      parameters{                
                      currentBuild() 
                      }                    
                }
            }
                
      }       
 }




buildFlowJob(under_folder+"/CreateJenkinsMaster") {
     displayName("Create Team Jenkins")
 
    parameters {
        stringParam('team','','Team Name')
        stringParam('Charge_Code','','Team Charge Code')
        labelParam('SharedSlave')
        {
        defaultValue('atg-toc-aws-slv01')
        }
             
    }  
   buildFlow(
        "build('/Infra/toc/CreateJenkinsMaster', team: params['team'],Charge_Code: params['Charge_Code'], SharedSlave: params['SharedSlave'])"
       )   
    
    configure { project -> project / 'buildNeedsWorkspace'('true') }
    
       publishers {
          downstreamParameterized {
                trigger('CreateJenkinStrategy') {
                      condition('UNSTABLE_OR_BETTER')  
                      parameters{                
                      currentBuild() 
                      }                    
                }
            }
                
      }          
 }
 
 
 
job(under_folder+"/CreateJenkinStrategy") {
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
            text(readFileFromWorkspace('Infra/toc/JenkinsMasterStrategy.include'))
        }
    }    
                
 }
 
 
     
deliveryPipelineView(under_folder+'/Jenkins-Creation-Pipeline') {
    enableManualTriggers true
    pipelineInstances 5
    configure { view -> view / 'allowPipelineStart'('true') }
    configure { view -> view / 'allowRebuild'('true') }
    configure { view -> view / 'showTotalBuildTime'('true') }
    
    pipelines {
        component('Jenkins Creation Pipeline', initial_job)
    }
    }
