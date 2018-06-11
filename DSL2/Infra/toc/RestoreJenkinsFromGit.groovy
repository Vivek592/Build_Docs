
def under_folder = 'Infra/toc'
folder(under_folder)

def job_name = under_folder +'/RestoreJenkinsFromGit'

job(job_name) {
    description 'Restores Jenkins Master from git'
    logRotator {
      daysToKeep -1
      numToKeep 20
    }
    parameters {
        stringParam('JenkinsConfigRepo','git@git.aws.gha.kfplc.com:KITS/Jenkins-Configuration.git','Jenkins git lab repo')
        stringParam('team','','Name of Jenkins Instance')
         labelParam('SharedSlave')
        {
         defaultValue('atg-toc-aws-slv01')
        }
       
    }  
    
    concurrentBuild(true)
    
    steps {
    
      shell ('cd /deployment/ansible \n\
			  ansible-playbook -i is-inventory/group_hosts/${team}-hosts is-appsetup/playbooks/deployJenkinsMasterConfig.yml -e "inventory_group=jenkins instName=${team} jenkins_git_cfg_repo=${JenkinsConfigRepo}" -vv')
    }  
}
