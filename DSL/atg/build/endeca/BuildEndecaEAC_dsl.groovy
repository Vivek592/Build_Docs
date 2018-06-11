def top_folder = '/ATG'
def sub_folder = '/ATG/Build'
def under_folder = '/ATG/Build/Endeca'

folder(top_folder)
folder(sub_folder)
folder(under_folder)

job(under_folder +'/BuildEAC') {
    
  logRotator {
      daysToKeep 2
    }
    
    parameters {
        stringParam('branch','develop','Working branch')
        labelParam('ATGSlave')
       
    } 
    scm {
         git {
             remote {
                 url 'https://github.com/KITSGitHubAdmin/KITS-Endeca.git'
                 branch '${branch}'
                 credentials 'ca5be30b-657d-4c0d-8191-edc84a8ad31e'
             }          
        }
    }
    
    steps {
       shell( 
           'export APP_VERSION=1.0.0 \n'+
           '	echo BUILD_VERSION=${GIT_COMMIT} > eac-project/build_version.properties \n'+
           '	rm -f /tmp/eac-project.jar \n'+
           ' tar -czf /tmp/eac-project.tar eac-project --exclude ".git/*"  \n'+	
           '	curl --upload-file /tmp/eac-project.tar ${OLD_NEXUS_RELEASE_LOCATION}/com/endeca/eac-project/${APP_VERSION}/eac-project-${APP_VERSION}.war --user darwin:darwin ') 
    }
}
    
