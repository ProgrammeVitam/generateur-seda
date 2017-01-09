Fichiers de configuration
==========================

Modules activables 
------------------

Le fichier conf/playbook_BinaryDataObject.json permet de gérer les modules que l'on désire activer. On pourra noter les points suivants : 

  * si on désire désactiver l'identification de format (via Siegfried), il suffit de supprimer la section json du module "siegfried"
  * si on désire rejeter du SIP les fichiers inconnus de Siegfried, il suffit de rajouter le paramètre ignoreUnknownFile à true aux paramètres du module siegfried (ex: "parameters" :{ "binarydataobject" : "@@binarydataobject@@" ,"siegfriedURL" : "http://localhost:5138" , "ignoreUnknownFile" : "true"}) 
  * si on désire changer l'algorithme de Digest pour le calcul d'empreinte, il suffit de changer la valeur du paramètre digest.algorithm du module digest. Les valeurs possibles sont MD5, SHA1, SHA256, SHA384 et SHA512 (sans "-")
  * si on désire rejeter les fichiers ayant une extension contenant un caractère "url-encoded", il suffit de rajouter le paramètre ignoreSpecialCharExtension à true aux paramètres du module binaryDataObjectConstructor 

Fichier de configuration des ArchiveTransfert
---------------------------------------------

Ce fichier se nomme ArchiveTransferConfig.json et peut se trouver à 2 endroits 
  
  * dans le répertoire de configuration "global" (conf/ArchiveTransferConfig.json) 
  * à la racine de l'arborescence dont on désire générer un ArchiveTransferRequest

Si le fichier est présent aux 2 endroits, pour chaque champ de 1er niveau, le fichier à la racine de l'arborescence a priorité sur le fichier "global" du répertoire conf. Par contre, cette fusion n'est valable que pour les champs de 1er niveau (Ex: Pour CodeListVersions, il n'y a pas de fusions des différentes clés)

Un fichier d'exemple se trouve dans le répertoire conf/ fourni. 


Il y a 2 types de champs : les champs liés au schéma SEDA et les champs "techniques". 
Pour les champs liés au SEDA, on a : 

* Les champs "MessageIdentifier", "ArchivalAgency" , "TransferringAgency" sont obligatoires dans le SEDA et sont donc nécessaires pour le générateur SEDA
* Il en est de même pour les clés "ReplyCodeListVersion", "MessageDigestAlgorithmCodeListVersion" et "MessageDigestAlgorithmCodeListVersion" du bloc "CodeListVersions"
* Les champs "Comment" et "ArchivalAgreement" sont facultatifs (au sens SEDA et Vitam)
* Les champs "ManagementMetadata.OriginatingAgencyIdentifier".et "ManagementMetadata.SubmissionAgencyIdentifier" sont des extensions du SEDA pour Vitam et sont requis pour Vitam

Pour les champs techniques, on a : 
* ignore_patterns :  il s'agit d'un tableau Json contenant une liste de motifs pouvant être présents dans l'arborescence à scanner et que l'on ne désire par inclure dans le SIP final. Il est possible d'insérer des modifs. Il s'agit des motifs du batch Windows (et non d'une expression régulière). Ex: si on désire supprimer tous les fichiers ayant pour extension "exe", on utilisera le motif \*.exe


Fichier d'enrichissement des métadonnées 
----------------------------------------

Le fichier ArchiveUnitMetadata.json.example présent dans ce répertoire donne un apercu du format json attendu. 

Attention : 

  * Ce format de fichier est fourni à titre provisoire et peut être modifié dans des versions ultérieures. 
  * Les champs Date (ex: CreatedDate) sont vus par le générateur SEDA comme des chaînes de caractères et le générateur les prend telles quelles. Il est donc de la responsabilité des utilisateurs de fournir des dates au format correct.
  * Il est à noter que les méta-données descriptives suivantes ne peuvent pas être importées à ce jour :

    = CustodialHistory (non testé à ce jour)
    = Keyword (non testé à ce jour)
    = AuthorizedAgent, Addressee, Recipient (problème dans la gestion du type SEDA AgentType)

  * Les méta-données suivantes seront écrasées par le mécanisme de calcul automatique des dates : TransactedDate, StartDate et EndDate <---> remarque EVR : elles seront écrasées où ?

