### Certificate Expiry Exporter
This project is used for detect the certificate expiry date and send to the alert receiver.


### How to build the source code
- Build with maven
```
  mvn clean package
```

## How to deployment the application
- Run the application with oc command
```
  cd deploy
  oc create -f certs-expiry-template.yaml
  oc process certs-expiry-template -p APP_NAME=osp-public -p DEPLOYMENT_IMAGE=registry.ocp4.cn/redhat/cert-expiry-exporter:1.1 -p CHECK_HTTPS_URL=https://hupu.com |oc create -f -
  oc process certs-expiry-template -p APP_NAME=registry-mirror -p DEPLOYMENT_IMAGE=registry.ocp4.cn/redhat/cert-expiry-exporter:1.1 -p CHECK_HTTPS_URL=https://hupu.com |oc create -f -
    deployment.apps/registry-mirror created
    service/registry-mirror created
    servicemonitor.monitoring.coreos.com/registry-mirror created
```




