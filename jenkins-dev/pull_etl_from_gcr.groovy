def folderName = "0_PREREQUISITE"
def listItems = [
[name:"1-Update-ETL-version", jobname: '1-Update-ETL-version', ETL_VERSION: "latest"]
]
folder(folderName) {
    description('Folder contains all jobs related with image versioning from GCR')
}
for (LinkedHashMap item : listItems){
  def etl_version = item.ETL_VERSION

  job(folderName+'/'+item.name ) {    
    parameters {
            stringParam('ETL_VERSION', etl_version, '')
    }
        
    logRotator {
            numToKeep(10)
    }
    steps {
          shell('''\
#!/bin/bash +x
echo 'Configured version : '+ ${ETL_VERSION}
mkdir -p $JENKINS_HOME/image_version
echo ${ETL_VERSION}>$JENKINS_HOME/image_version/generic.database.etl.version
export ETL_VERSION=$(cat $JENKINS_HOME/image_version/generic.database.etl.version)
''')
    }
  }
}
