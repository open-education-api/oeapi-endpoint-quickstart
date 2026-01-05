# Additional API calls intended to support the frontend (not part of the specification)

## Introduction

The quickstart includes several API calls designed to support frontend development. Most of these calls are intended to provide the frontend with valid or allowed values for certain enumerations _(such as mode of delivery, fields of study, etc.)_.

This approach allows frontend selectors to be populated dynamically with values provided by the endpoint, rather than hard-coding them in the frontend itself. It also simplifies maintenance, since values can be added, corrected, or updated on the backend without requiring frontend changes.


### Enumerators  

The quickstart endpoint provides access to the following valid enumeration values:

* modeOfDelivery : at endpointURL + "/enumerator?enum=modeOfDeliveryType"
* levelType: at endpointURL + "/enumerator?enum=levelType"
* studyLoadType: at endpointURL + "/enumerator?enum=studyLoadType"

### Fields of study

The specification requires the use of standards for specifying Fields of Study (e.g [International Standard Classification of Education: fields of education and training 2013 (ISCED-F 2013) detailed field descriptions](https://unesdoc.unesco.org/ark:/48223/pf0000235049) ) 

This classification is defined at different levels of granularity:
Level 1 uses broad categories, while Level 3 provides the most detailed classification.

* You can get possible values at: endpointURL + /fieldsofstudy?level=1 (where level=1, indicates to retrieve level 1 values)


### Security
 
If you want to check the security status of your quickstart endpoint (i.e. whether authentication is required for update operations), you can use the following endpoint:

* You can get Security Status at endpointURL + "/auth/secStatus"


