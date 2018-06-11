
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/CreateTeamHostFile'

job(job_name) {
    description 'Creates Teams host file'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('team','',' Jenkins Instance')
        labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
	    shell ('cd /deployment/ansible/is-inventory/group_hosts ; cp team-hosts ${team}-hosts ; sed -i -e \'s/envName="team"/envName="\'${team}\'"/g\' ${team}-hosts')
        }  
}
