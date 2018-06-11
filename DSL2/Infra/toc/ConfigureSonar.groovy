
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/ConfigureSonar'

job(job_name) {
    description 'Creates a Sonar'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('rds_db_name','','MySQL DB Name')
        stringParam('rds_instance_name','${rds_db_name}','MySQL DB Instance Name')
        stringParam('team','','Team name..')
        stringParam('rds_user','mysqladmin','DB admin User name')
        stringParam('rds_passwd','ChangeMeNow','DB Password')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/configureSonar.yml -e "inventory_group=sonar rds_user=${rds_user} rds_passwd=${rds_passwd} rds_db_name=${rds_instance_name} sonarver=sonarqube-5.4 action=configure" -vv')
    }  
}



job(under_folder +'/StartOrStopSonar') {
    description 'Start / Stop  Sonar'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('team','','Team name..')
        choiceParam('action',[' ','start','stop'],'stop / start')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/configureSonar.yml -e "inventory_group=sonar action=${action} sonarver=sonarqube-5.4" -vv')
    }  
}
