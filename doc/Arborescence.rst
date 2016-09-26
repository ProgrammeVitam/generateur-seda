Générateur de SEDA à partir d'une arborescence de fichiers 
==========================================================

Objectif de l'outil
-------------------

Dans le cadre de projet Vitam, le besoin de générer des fichiers XML respectant le format SEDA 2.0 (http://www.archivesdefrance.culture.gouv.fr/seda/) de manière semi-automatique (au delà d'editeurs XML comme oxygen ou Eclipse) pour : 

* des besoins de tests des développements 
* éventuellement fournir des outils dans la toolbox Vitam pour faciliter l'intégration dans Vitam

Nous partirons sur un premier version dite PP qui implémentera les spécifications suivantes 

Arborescence d'entrée
---------------------
Sous Windows, l'archiviste a préparé un arborescence avec le formalisme suivant :

* L'arborescence des répertoire représente les relations entre les archive units et les data-object groups
 
  + Du fait de la réprésentation arborescente des systèmes d'exploitations, on se limitera à un arbre (et non un DAG dans le cadre général) pour les relations entre archive unit et data-object group

* Dans l'arborescence, on peut trouver des répertoires :

  + Répertoire dont le nom commence par __ et termine par __ , il s'agit d'un DataObjectGroup qui est rattaché à un ArchiveUnit virtuel de même nom (A valider)
  
    - Ce répertoire ne doit contenir que des fichiers . En cas de présence d'un répertoire, il s'agit d'une erreur bloquante lors du parsing de l'arborescence
    - Les fichiers dans ce répertoire doivent avoir la forme suivante .

        - <Usage du SIP>_<Version du SIP>_<nom du fichier
        - Contrairement à l'entrée Vitam, la version du SIP ne peut pas être sous entendu si la version est 1 
        - Il n'y a pas de vérification sur l'adéquation des "Usages" par rapport à un référentiel

    - Si un fichier n'a pas le bon formalisme dans ce répertoire, il est ignoré
    - Le Title de l'archiveUnit associé à ce dataobjectgroup est le nom du répertoire auquel on retranche les __ de début et de fin
    - L'archive Unit Virtual a le champ DescriptionLevel à Item
	
  + Répertoire dans le cadre général
  
    - Le nom du répertoire est le champ Content/Title de l'archive unit
    - Les métadonnées de gestions seront une balise vide (<Management/>)
    - L'archive Unit dans le cas général a le champ DescriptionLevel à RecordGrp
	
  + Répertoire qui n'est pas un dataobjetgroup et contenant des fichiers
  
    - il s'agit d'une Archive Unit ayant virtuellement par fichier présent 1 archive Unit qui contient lui-même 1 DataObjectGroup 

	- L'archive Unit Virtual a le champ DescriptionLevel à Item (A valider)
	- L'archive Unit Virtual a le champ transactedDate à Modification time du fichier (Non Implémenté)
	- Un fichier vide est ignoré en logguant la présence de ce fichier vide mais il ne s'agit que d'un warning (non bloquant) (A valider)

    - Un fichier caché (au sens attribut Windows) est ignoré (Non implémenté)	

* Dans l'arborescence, on peut trouver des fichiers :

  + Les noms de fichiers suivants sont ignorés : 

    - Le fichier ArchiveTransferConfig.json à la racine de l'arborescence. Ce fichier contient les paramétrages globaux spécifiques pour cette arborescence
    - Le fichier ArchiveUnitMetadata.json sur chaque répertoire . Ce fichier contient les métadonnées descriptives pour l'ArchiveUnit correspond au répertoire auquel il appartient

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


Sortant de l'outil
------------------


2 fichiers : 
 * Un fichier horodaté ZIP (pkZIP) avec les fichiers suivants (SIP-yyyyMMddHHmmss.zip) : 
   * Un fichier SEDA généré qui est valide par rapport au schéma SEDA 2.0 (MENODA) : L'affichage des archive Unit est dans l'ordre du parcours en profondeur de l'arborescence
   * Un répertoire Content qui contient l'ensemble des BinaryDataObject décrits dans le fichier SEDA. Chaque fichier a pour nom son ID dans le bordereau Seda
 * Un fichier avec la liste des fichiers rejetés (SIP-yyyyMMddHHmmss.rejected) 


Couverture du SEDA 
------------------

Dans le fichier SEDA, les Champs suivants sont gérés : 

 * ArchiveUnit : Comment,MessageIdentifier,ArchivalAgreement,CodeListVersions,ArchivalAgency.Identifier,TransferringAgency.Identifier sont gérables par un fichier json fourni en paramètre
 * DataObjectPackage.BinaryDataObject 
 
   + DataObjectGroupId : généré programmatiquement
   + DataObjectVersion : fixé arbitrairement à DataObjectVersion0
   + Uri : Content/<ID du Binary DataObject>.<extension initiale> (si le fichier n'a pas d'extension initiale, l'extension .seda est rajoutée)
   + MessageDigest : Fourni l'empreinte en SHA-512 (l'algorithme est paramétrable)
   + Size : Fourni la taille du fichier
   + FormatIdentification : si le module Siegfried est activé, on positionne les 3 champs FormatLitteral,MimeType,FormatId
   + FileInfo : FileName et LastModified (mtime du fichier)

 * DataObjectPackage.Management : élément vide
 * DataObjectPackage.DescriptiveMetadata.ArchiveUnit.Content :
 
   + DescriptionLevel : Item s'il y a un DOG comme fils , RecordGrp sinon
   + Title : Nom du fichier ou répertoire
   + Description : un champ de debug pour l'instant
   + TransactedDate : Pour les archiveUnit de type Item (père d'un DOG), il s'agit de la date du dernier BinaryDataObject entré dans l'ArchiveUnit. Il s'agit d'un comportement non cible mais il reste à définir le comportement dans les différents cas de répertoires de type "DOG" (avec "__" au début et à la fin de répertoires)
   + StartDate/EndDate : Pour les ArchiveUnit de type RecordGrp, le StartDate est la TransactedDate la plus ancienne des fichier du RecordGrp et la EndDate est la TransactedDate la plus récente des fichiers du RecordGrp
