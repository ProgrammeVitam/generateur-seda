Générateur de SEDA à partir d'une arborescence de fichiers 
==========================================================

Objectif de l'outil
-------------------

Dans le cadre de projet Vitam, il est nécessaire de générer des SIP sous forme de fichiers XML, conformes au standard SEDA 2.0 (http://www.archivesdefrance.culture.gouv.fr/seda/) de manière semi-automatique (au delà d'editeurs XML comme oxygen ou Eclipse) pour : 

* tester les développements 
* éventuellement faciliter l'intégration dans Vitam en fournissant des outils dans la toolbox Vitam

Arborescence d'entrée
---------------------
Sous Windows, l'archiviste a préparé une arborescence avec le formalisme suivant :

* L'arborescence des répertoires représente les relations entre les ArchiveUnits et les DataObjectGroup au sens du standard SEDA 2.0.
 
  + Du fait de la réprésentation arborescente des systèmes d'exploitation, on se limitera à un arbre (et non un DAG dans le cadre général) pour les relations entre ArchiveUnit et DataObjectGroup

* Dans l'arborescence, on peut trouver des répertoires :

  + Répertoire dont le nom commence par __ et termine par __ , il s'agit d'un DataObjectGroup qui est rattaché à un ArchiveUnit virtuel de même nom 
  
    - Ce répertoire ne doit contenir que des fichiers. En cas de présence d'un répertoire, il s'agit d'une erreur bloquante lors du parsing de l'arborescence
    - Les fichiers dans ce répertoire doivent avoir la forme suivante .

        - <Usage du SIP>_<Version du SIP>_<nom du fichier> . Remarque : le nom du fichier peut inclure une extension (si l'extension est manquante, l'extension .seda sera ajoutée)
        - Contrairement à l'entrée Vitam, la version du SIP ne peut pas être sous-entendue si la version est 1 <------> remarque EVR : pas clair
        - Il n'y a pas de vérification sur l'adéquation des "Usages" par rapport à un référentiel

    - Si un fichier n'a pas le bon formalisme dans ce répertoire, il est ignoré
    - Le Title de l'ArchiveUnit associé à ce DataObjectGroup est le nom du répertoire auquel on retranche les __ de début et de fin
    - L'ArchiveUnit virtuel a pour valeur du champ DescriptionLevel "Item"

  + Répertoire dans le cadre général
  
    - Le nom du répertoire est le champ Content/Title de l'ArchiveUnit
    - Les métadonnées de gestion seront une balise vide (<Management/>)
    - L'ArchiveUnit correspondant à ce répertoire a par défaut pour valeur du champ DescriptionLevel "RecordGrp"

  + Fichiers présents dans un répertoire "standard" (ne commencant par __ et ne terminant pas par __ ) : 
  
    - il s'agit d'une ArchiveUnit ayant virtuellement par fichier présent 1 ArchiveUnit qui contient lui-même 1 DataObjectGroup 

	- L'ArchiveUnit virtuel a pour valeur du champ DescriptionLevel "Item" 
	- L'ArchiveUnit Virtuel a pour valeur du champ TransactedDate la date de dernière modification du fichier (Modification time du fichier)
	- Un fichier vide est ignoré en logguant la présence de ce fichier vide mais il ne s'agit que d'un warning (non bloquant) 
    - Un fichier caché (au sens attribut Windows) est ignoré (Non implémenté a ce jour)
    - Dans le binaryDataObject, la valeur du champ DataObjectVersion est par défaut "BinaryMaster". Si le fichier est de la forme __<Lettres minuscules ou majuscule>_<Chiffres>_.*, le champ DataObjectVersion vaut <Lettres minuscules ou majuscule>_<Chiffres> . Si le fichier est de la forme __<Lettres minuscules ou majuscule>_.*; le champ DataObjectVersion vaut <Lettres minuscules ou majuscule>_<Chiffres>

* Dans l'arborescence, on peut trouver des fichiers : 

  + Les noms de fichiers suivants sont ignorés : 

    - Le fichier ArchiveTransferConfig.json à la racine de l'arborescence. Ce fichier contient les paramétrages globaux spécifiques pour cette arborescence
    - Le fichier ArchiveUnitMetadata.json sur chaque répertoire . Ce fichier contient les métadonnées descriptives pour l'ArchiveUnit correspond au répertoire auquel il appartient
    - Le fichier ArchiveUnitMetadata.xml sur chaque répertoire . Ce fichier contient les métadonnées descriptives pour l'ArchiveUnit correspond au répertoire auquel il appartient
  
  + En cas de présence d'un fichier ArchiveUnitMetadata.xml et d'un fichier ArchiveUnitMetadata.json dans le même répertoire

Example d'arborescence
----------------------

Arborescence sources
^^^^^^^^^^^^^^^^^^^^
:: 

  Répertoire : /A
  Fichier    : /A/a1
  Fichier    : /A/a2
  Fichier    : /A/ArchiveTransferConfig.json (ignoré car fichier de paramètre)
  Répertoire : /A/B
  Répertoire : /A/B/ArchiveUnitMetadata.json (ignoré dans le SEDA cible car fichier de paramètre)
  Fichier    : /A/B/b1
  Répertoire : /A/__C__
  Fichier    : /A/__C__/c1
  Fichier    : /A/__C__/c2
  Répertoire : /A/D
  Répertoire : /A/D/E

SEDA (sans les BinaryDataObject)
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

::

  ArchiveUnit     : A (racine)
  ArchiveUnit     : a1 (père : A )
  DataObjectGroup : dog_a1 (père:  a1), contient le BinaryDataObject a1
  ArchiveUnit     : a2 (père : A )
  DataObjectGroup : dog_a2 (père:  a2), contient le BinaryDataObject a2
  ArchiveUnit     : B (père : A) enrichi avec les méta-données présentes dans /A/B/ArchiveUnitMetadata.json
  ArchiveUnit     : b1 (père : B)
  DataObjectGroup : dog_b1 (père:  b1), contient le BinaryDataObject b1
  ArchiveUnit     : C (père : A)
  DataObjectGroup : dog_C (père:  C), contient les BinaryDataObject c1 et c2
  ArchiveUnit     : D (père : A)
  ArchiveUnit     : E (père : D)

Couverture du SEDA 
------------------

Dans le fichier SEDA, les champs suivants sont gérés : 

 * ArchiveTransfer : les champs Comment, MessageIdentifier, ArchivalAgreement, CodeListVersions, ArchivalAgencyIdentifier, TransferringAgencyIdentifier sont configurables (via le fichier ArchiveTransferConfig.json) . Voir le fichier doc/Configuration.rst pour plus d'informations 
 * DataObjectPackage.BinaryDataObject 
 
   + DataObjectGroupId : généré programmatiquement
   + DataObjectVersion : fixé arbitrairement à BinaryMaster si le fichier n'est pas sous la forme <Usage du SIP>_<Version du SIP>_<nom du fichier> 
   + Uri : Content/<ID du Binary DataObject>.<extension initiale> (si le fichier n'a pas d'extension initiale, l'extension .seda est rajoutée)
   + MessageDigest : fournit l'empreinte en SHA-512 (l'algorithme est paramétrable)
   + Size : fournit la taille du fichier
   + FormatIdentification : si le module Siegfried est activé, on positionne les 3 champs FormatLitteral, MimeType, FormatId
   + FileInfo : FileName et LastModified (mtime du fichier)

 * DataObjectPackage.ManagementMetadata : Les champs suivants (extension du SEDA pour Vitam) sont gérés) : 

   + OriginatingAgencyIdentifier : identifiant du service producteur
   + SubmissionAgencyIdentifer : identifiant du service versant

 * DataObjectPackage.DescriptiveMetadata.ArchiveUnit.Content :
 
   + DescriptionLevel : Item s'il y a un DataObjectGroup comme fils, RecordGrp sinon
   + Title : Nom du fichier ou répertoire
   + Description : Chemin complet du fichier ou répertoire associé
   + TransactedDate : pour les archiveUnit de type Item (père d'un DataObjectGroup), il s'agit de la date du dernier BinaryDataObject entré dans l'ArchiveUnit. Il s'agit d'un comportement non cible mais il reste à définir le comportement dans les différents cas de répertoires de type "DataObjectGroup" (avec "__" au début et à la fin du répertoire)
   + StartDate/EndDate : pour les ArchiveUnit de type RecordGrp, le StartDate est la TransactedDate la plus ancienne des fichiers du RecordGrp et la EndDate est la TransactedDate la plus récente des fichiers du RecordGrp

Pour DataObjectPackage.DescriptiveMetadata.ArchiveUnit.Content, il est possible de surcharger ces méta-données via la mise en place d'un fichier ArchiveUnitMetadata.json dans le répertoire correspondant à l'archiveUnit. Voir le fichier doc/Configuration.rst pour plus d'informations

