### Certificate Expiry Exporter
This project is used for detect the certificate expiry date and send to the alert receiver.


### How to build the source code
- Build with maven
```
  mvn clean package
```

## How to deployment the application
- Open the Operator Hub in Web Console. Search for the "Cert Utils Operator" and click on the "Install" button.
  
- Silence `CertificateApproachingExpiration` and `CertificateIsAboutToExpire` alerts

  * Navigate to the OpenShift Web Console.
  * Go to Observe → Alerting in the left-hand navigation menu.
  * Locate the Silence tab and click on Create Silence.
  * Duration: Silence alert from... Now, for -, Until 2099/01/01 00:00:00.
  * Alert labels: Label name is `alertname`, Label value is `CertificateApproachingExpiration` or `CertificateIsAboutToExpire`.
  * Info comment: Silence the alert for the certificate expiry.
  * Click on the Silence button.
  * Continue to create a silence for the other alert.

- Run the application installation with oc command
```
  cd deploy
  oc new-project cert-expiry
  oc label ns certs-expiry openshift.io/cluster-monitoring="true"
  oc adm policy add-role-to-user view system:serviceaccount:openshift-monitoring:prometheus-k8s -n certs-expiry
  oc create -f certificate-rule-alerts.yaml
  oc create -f certs-expiry-template.yaml
  oc process certs-expiry-template -p APP_NAME=osp-public -p DEPLOYMENT_IMAGE=<actual image address> -p CHECK_HTTPS_URL=https://<osp public URL> |oc create -f -
  oc process certs-expiry-template -p APP_NAME=registry-mirror -p DEPLOYMENT_IMAGE=<actual image address> -p CHECK_HTTPS_URL=https://<registry mirror URL> |oc create -f -

```





