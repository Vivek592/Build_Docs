
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/StopOrStartJenkins'

job(job_name) {
    description 'Stops or Starts a Tomcat'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        choiceParam('state',['','running','stopped'],'State of Tomcat to be')
        stringParam('team','','Jenkins master to action')
         labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
         ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/setTomcatState.yml -e "inventory_group=jenkins tomcatState=${state}"') 
       }  
}
