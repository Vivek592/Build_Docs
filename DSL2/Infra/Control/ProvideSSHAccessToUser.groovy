def top_folder = '/Infra'
def under_folder = '/Infra/Control'
def under_folder2 = '/Infra/Control/ssh'
def under_folder3 = '/Infra/Control/VPN'

folder(top_folder)
folder(under_folder)
folder(under_folder2)
folder(under_folder3)

job(under_folder2 +'/ProvideSSHAccessToUser') {
    
    parameters {
        stringParam('user_name')
        stringParam('ENV_PREFIX','','environment identifier')
        choiceParam('inventory_group', ['storefront','agent','auxagent','auxstorefront','auxindex','fulfillment','lockagent','lockstorefront','publishing','staging','tomcat','itl','mdex','slaveATG','web'])
        choiceParam('user_group', ['jboss','tomcat','endeca','apache'],'which group the user belongs to? \nfor ecommadm groupd user jboss \nfor itl mdex use endeca \nfor tomcat user tomcat')
        labelParam('DeploymentSlave')
        choiceParam('delete', ['no','yes'],'Set yes to delete user')
        choiceParam('expires', ['1','2','10','100','180','1000'],'Expiry of the user in days') 
        
    } 
 
    concurrentBuild(true)

    steps {
        shell('cd /deployment/ansible \n'+
	      'ansible-playbook -i /deployment/ansible/is-inventory/group_hosts/${ENV_PREFIX}-hosts is-iaas/playbooks/createDevUser.yml -e "inventory_group=${inventory_group} usr_name=${user_name} usr_password=test1234 usr_set_password=yes group_name=${user_group} usr_remove=${delete} expires=${expires}" -vvvv \n')  
    }
}

