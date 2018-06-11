def top_folder = '/Infra'
def under_folder = '/Infra/Control'
def under_folder2 = '/Infra/Control/ssh'
def under_folder3 = '/Infra/Control/VPN'

folder(top_folder)
folder(under_folder)
folder(under_folder2)
folder(under_folder3)

job(under_folder +'/StopOrStartAWSEnvironment') {
    
    parameters {
        stringParam('DeploymentSlave','atg-shared-aws-slv03')
        choiceParam('action', ['stopped','running'])
        choiceParam('tag_name', ['Environment','Name','Group'])
        stringParam('tag_value','','Value of tag to idenfity the EC2 instance \nFor example : \nEnvironment=devops04\nAvailability=working_hours')
        stringParam('wait_for_server_startup','180')
        
    } 
 
    concurrentBuild(true)

    steps {
        shell('if [ "$tag_value" == "" ];then \n'+
              '   exit 1 \n'+ 
              'fi \n\n'+ 
              'cd /deployment/ansible \n'+ 
              'ansible-playbook is-iaas/playbooks/ec2_stop-start.yml -e "ec2action=${action} tag_name=Name tag_value=atg-${tag_value}-aws-nfs01" -vv \n\n'+ 
              'if [ "$action" == "running" ];then \n'+ 
              ' echo "waiting for 3 minutes for nfs to initialise" \n'+ 
              ' sleep ${wait_for_server_startup} \n'+ 
              'fi \n\n'+
              'ansible-playbook is-iaas/playbooks/ec2_stop-start.yml -e "ec2action=${action} tag_name=${tag_name} tag_value=${tag_value}" -vv \n\n'+ 
              'if [ "$action" == "running" ];then \n'+ 
              ' echo "waiting for 3 minutes for all servers to initialise" \n'+ 
              ' sleep ${wait_for_server_startup} \n'+ 
	      'fi \n')  
    }
}

