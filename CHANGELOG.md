## 0.15.0

* Parametrization of the global ManagementMetadata block 
* Parametrization of the Management block (in ArchiveUnit section)
* Raw addition of Content and Management block (in ArchiveUnit section)

## 0.14.0

* Add the binary-generator in the testing-module
* Rework on the packaging solution
* Add the possibility to have ArchiveUnits sons a "DOG ArchiveUnit"

## 0.11.1

* Add the license file (CeCill and CC-By-SA)

## 20161209

* Add the parameter ArchiveTransferConfig in the binaryDataObjectConstructor module so that file with a special character in the extension will be rejected. For retrocompatibility, this feature is not activated by default
* For Windows installation, manage the installation in a directory where the full path contains an 'space' character
* Update the documentation

## 20161109

* Add the support of SubmissionAgencyIdentifier and OriginatingAgencyIdentifier fields (extension of ManagementMetadata for Vitam)
* Add the parameter ignoreUnknownFile in the SiegfriedModule so that the unknown file are not included in the SIP (in Vitam, an unknown file in a SIP causes a "business refusal" to all the SIP). For retro-compatibilité, this feature is not activated by default
* Update the documentation

## 20161018

* Changed from SiegFried 1.5.0 64 bits to 1.6.5 32 bits (to have compatibility with 32 bits OS)
* Update the Pronom Definition for SiegFried to V88
* Prevent a NPE during the calculation of StartDate/EndDate
* Bumps to 0.10.0-SNAPSHOT for the Generator version and for the dependancies to Vitam common library 

## 20161010

* First published version
