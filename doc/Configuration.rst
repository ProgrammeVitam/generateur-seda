Fichiers de configuration
=========================

Modules activables 
------------------

Le fichier `conf/playbook_BinaryDataObject.json`_ permet de gérer les modules que l'on désire activer. On pourra noter les points suivants : 

* si on désire désactiver l'identification de format (via Siegfried), il suffit de supprimer la section json du module "siegfried"
* si on désire rejeter du SIP les fichiers inconnus de Siegfried, il suffit de passer le paramètre ignoreUnknownFile à true dans la section relative aux paramètres du module siegfried (ex: ``"parameters" :{ "binarydataobject" : "@@binarydataobject@@" ,"siegfriedURL" : "http://localhost:5138" , "ignoreUnknownFile" : "true"}``)
* si on désire changer l'algorithme de Digest pour le calcul d'empreinte, il suffit de changer la valeur du paramètre digest.algorithm du module digest. Les valeurs possibles sont MD5, SHA1, SHA256, SHA384 et SHA512 (sans "-")
* si on désire rejeter les fichiers ayant une extension contenant un caractère "url-encoded", il suffit de passer le paramètre ignoreSpecialCharExtension à true dans la section relative aux paramètres du module binaryDataObjectConstructor

Fichier de configuration des ArchiveTransfer
--------------------------------------------

Ce fichier se nomme ArchiveTransferConfig.json et peut se trouver à 2 endroits  :

* dans le répertoire de configuration "global" (``conf/ArchiveTransferConfig.json``)
* à la racine de l'arborescence dont on désire générer un ArchiveTransferRequest

Si le fichier est présent aux 2 endroits, pour chaque champ de 1er niveau, le fichier à la racine de l'arborescence a priorité sur le fichier "global" du répertoire conf. Par contre, cette fusion n'est valable que pour les champs de 1er niveau (Ex: Pour CodeListVersions, il n'y a pas de fusions des différentes clés)

Un fichier d'exemple se trouve dans le répertoire ``conf/`` fourni.

Il y a 2 types de champs : les champs liés au schéma SEDA et les champs "techniques".

Pour les champs liés au SEDA, on a :

* Les champs "MessageIdentifier", "ArchivalAgency" , "TransferringAgency" sont obligatoires dans le SEDA et sont donc nécessaires pour le générateur SEDA
* Il en est de même pour les clés "ReplyCodeListVersion", "MessageDigestAlgorithmCodeListVersion" et "MessageDigestAlgorithmCodeListVersion" du bloc "CodeListVersions"
* Les champs "Comment" et "ArchivalAgreement" sont facultatifs (au sens SEDA et Vitam)
* Le champ "ManagementMetadata" est facultatifs. Son formalisme est identique à celui de la section Management du fichier `ArchiveUnitMetadata.json`_ . 
* Les champs "ManagementMetadata.OriginatingAgencyIdentifier".et "ManagementMetadata.SubmissionAgencyIdentifier" sont des extensions du SEDA pour Vitam. Le premier est obligatoire pour Vitam.

Pour les champs techniques, on a :

* ignore_patterns :  il s'agit d'un tableau Json contenant une liste de motifs pouvant être présents dans l'arborescence à scanner et que l'on ne désire par inclure dans le SIP final. Il est possible d'insérer des modifications. Il s'agit des motifs du batch Windows (et non d'une expression régulière). Ex: si on désire supprimer tous les fichiers ayant pour extension "exe", on utilisera le motif ``\*.exe``


Fichier d'enrichissement des métadonnées 
----------------------------------------

Le fichier `ArchiveUnitMetadata.json`_ présent dans ce répertoire donne un apercu du format json attendu. 

Il y a 3 sections dans ce fichier :

+ Section Content (pour les métadonnées descriptives) : 

  * Ce format de fichier est fourni à titre provisoire et peut être modifié dans des versions ultérieures. 
  * Les champs Date (ex: CreatedDate) sont vus par le générateur SEDA comme des chaînes de caractères et le générateur les prend telles quelles. Il est donc de la responsabilité des utilisateurs de fournir des dates au format correct.
  * Il est à noter que les métadonnées descriptives suivantes ne peuvent pas être importées à ce jour :

    - CustodialHistory (non testé à ce jour)
    - Keyword (non testé à ce jour)
    - AuthorizedAgent, Addressee, Recipient (problème dans la gestion du type SEDA AgentType)

  * Les métadonnées suivantes seront écrasées par le mécanisme de calcul automatique des dates : TransactedDate, StartDate et EndDate
+ Section Management (pour les méta-données de gestion) : L'ensemble du SEDA est géré via le formalisme JSON en dehors des extensions (OtherManagementAbstract) 

+ Section VitamFather (pour le rattachement d'une unité archivistique à une unité existante)
  * Le champ `id` contient l'identifiant de l'unité archivistique parente
  * les balises `Title` et `DescriptionLevel` doivent reprendre les informations de l'unité archivistique parente.


Rattachement à une unité archivistique existante
------------------------------------------------

Le générateur seda permet également de générer un fichier AU FORMAT SEDA qui peut attacher une nouvelle unité archivistique à une unité archivistique existante (qu'il s'agisse d'une AU lié à un arbre, à un plan ou une AU classique).

Pour celà, il faut renseigner dans l'unité archivistique fille l'identifiant unique de son parent (le GUID).

Il faut aussi renseigner les champs `Title` et `DescriptionLevel` qui sont obligatoires dans la norme SEDA.

Un exemple de configuration est donné dans le fichier `ArchiveUnitMetadata.json` (voir section précédente).

.. Ce document est distribué sous les termes de la licence Creative Commons Attribution - Partage dans les Mêmes Conditions 3.0 France (CC BY-SA 3.0 FR)
