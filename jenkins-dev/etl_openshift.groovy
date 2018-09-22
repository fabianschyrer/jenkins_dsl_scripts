def folderName = "ETL"
def listItems = [
[name:"extract-postgresql-sample", jobname: 'postgresql-sample', profile:"th-oc\\/distributors", service_account:"gcp-gcs.json", secret_key:"gcp-gcs", mode:'daily', start_date: "yesterday", end_date: "today",retry_build:1]
]
folder(folderName) {
    description('Folder contains all jobs related with postgresql-sample on openshift')
}
for (LinkedHashMap item : listItems){
    def env = item.env
    def profile = item.profile
    def jobname = item.jobname
    def output_gcs_bucket = item.output_gcs_bucket
    def mode = item.mode
    def modeChoise = []
    def start_date = item.start_date
    def end_date = item.end_date
    def service_account = item.service_account
    def secret_key = item.secret_key
    if(item.mode == 'daily'){
        modeChoise.add('daily')
    }
    else if(item.mode == 'ondemand'){
        modeChoise.add('daily')
        modeChoise.add('monthly')
    }
    else{
        modeChoise.add('daily')
        modeChoise.add('monthly')
    }

    job(folderName+'/'+item.name ) {        
        parameters {
            stringParam('PROFILE', profile, '')
            stringParam('START_DATE', start_date, '')
            stringParam('END_DATE', end_date, '')
            stringParam('SERVICE_ACCOUNT', service_account, '')
            stringParam('SECRET_KEY', secret_key, '')
        }
        logRotator {
            numToKeep(10)
        }
        if (item.trigger_cron != null){
            triggers {
                cron(item.trigger_cron)
            }
        }
        steps {
            shell('''\
export ETL_VERSION=$(cat $JENKINS_HOME/image_version/generic.database.etl.version)
sed "s/%BUILD_NUMBER%/${BUILD_NUMBER}/g" ~/template/etl-template.yaml \
| sed "s/%JOB_BASE_NAME%/${JOB_BASE_NAME}/g" \
|sed "s/%START_DATE%/${START_DATE}/g" \
|sed "s/%END_DATE%/${END_DATE}/g" \
|sed "s/%SERVICE_ACCOUNT%/${SERVICE_ACCOUNT}/g" \
|sed "s/%SECRET_KEY%/${SECRET_KEY}/g" \
|sed "s/%ETL_VERSION%/${ETL_VERSION}/g" \
|sed "s/%PROFILE%/${PROFILE}/g"> etl-${JOB_BASE_NAME}-${BUILD_NUMBER}.yaml

cat etl-${JOB_BASE_NAME}-${BUILD_NUMBER}.yaml
oc create -f etl-${JOB_BASE_NAME}-${BUILD_NUMBER}.yaml

#Wait until the pods is running, within 5 mins timeout
POD_NAME=etl-${JOB_BASE_NAME}-${BUILD_NUMBER}
for i in {1..60}
do
  POD_PHASE=$(oc get po "$POD_NAME" --template={{.status.phase}})
  if [ "$POD_PHASE" = "Running" ]
  then
    echo "Pod is Running : attached output"
    break;
  else
    echo "wait 5s"
    sleep 5s;
  fi  
done

oc logs -f etl-${JOB_BASE_NAME}-${BUILD_NUMBER}
oc delete -f etl-${JOB_BASE_NAME}-${BUILD_NUMBER}.yaml
rm etl-${JOB_BASE_NAME}-${BUILD_NUMBER}.yaml
''')
        }
    }
}
