def top_folder = '/ATG'
def sub_folder = '/ATG/Build'
def under_folder = '/ATG/Build/KF'

folder(top_folder)
folder(sub_folder)
folder(under_folder)

job(under_folder +'/BuildKFCore') {
    
    logRotator {
        daysToKeep -1
        numToKeep 30
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('ATGSlave')
    } 
    scm {
        git {
            remote {
                url 'http://git.aws.gha.kfplc.com/KITS/ATG-App-KF-Base-Dev.git'           
                branch '${branch}'
                credentials 'efce5953-3a01-4bcd-b4ad-da13c22f712a'
            }          
        }
    }
    
    steps {
        shell('export PATH=${RUBY_HOME}:/$PATH \n'+
		'export JAVA_HOME=${JDK_VER_TO_USE} \n'+
		'export ATG_HOME=${ATG_HOME} \n'+
		'export JBOSS_HOME=/tmp \n'+
		'cd KF \n'+
		'buildr clean install -e sqa4') 
    }
}

def release_folder=under_folder+"/Release" 
def local_repository='/app/ecomm/repository/'   
folder(release_folder) 
job(release_folder +'/TagReleaseKF') {
    
    logRotator {
        daysToKeep -1
        numToKeep 30
    }
    
    parameters {
        stringParam('artifact_version')
        labelParam('BuildSlave')
    } 

    steps {
        downstreamParameterized {
            trigger('/Common/UploadArtifactToNexus') {
                block {
                    buildStepFailure('FAILURE')
                    failure('FAILURE')
                    unstable('UNSTABLE')
                }
                parameters{
                    predefinedProp('artifact_name','kf-module-${artifact_version}.zip')
		    predefinedProp('artifact_location','/deployment/ecomm/jenkins-slave/workspace/ATG/Build/KF/BuildKFCore/KF/Pack-All-Modules/target/')
		    predefinedProp('nexus_location','kf/atg/module/kf/kf-module')                      
                    currentBuild() 
                }                             
            }
        }
    }
}
    
