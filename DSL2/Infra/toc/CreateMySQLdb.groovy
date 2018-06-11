
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/CreateMySQLdb'

job(job_name) {
    description 'Creates a MySQL DB'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('Charge_Code','','Charge code for the environment')
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
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-iaas/playbooks/createMysqlDB.yml -e "inventory_group=mysql  chargeCode=${Charge_Code} rds_db_name=${rds_db_name} rds_instance_name=${rds_instance_name} rds_user=${rds_user} rds_passwd=${rds_passwd}" -vvvv')
    }  
}
