Générateur de SEDA à partir d'une arborescence de fichiers 
==========================================================

Objectif de l'outil
-------------------

Dans le cadre de projet Vitam, le besoin de générer des fichiers XML respectant le format SEDA 2.0 (http://www.archivesdefrance.culture.gouv.fr/seda/) de manière semi-automatique (au delà d'editeurs XML comme oxygen ou Eclipse) pour : 

* des besoins de tests des développements 
* éventuellement fournir des outils dans la toolbox Vitam pour faciliter l'intégration dans Vitam

Nous partirons sur un premier version dite PP qui implémentera les spécifications suivantes 

Entrant de cet outil 
--------------------
Sous Windows, l'archiviste a préparé un arborescence avec le formalisme suivant :

* L'arborescence des répertoire représente les relations entre les archive units et les data-object groups
 
  + Du fait de la réprésentation arborescente des systèmes d'exploitations, on se limitera à un arbre (et non un DAG dans le cadre général) pour les relations entre archive unit et data-object group

* Dans l'arborescence, on peut trouver des répertoires :

  + Répertoire dont le nom commence par __ et termine par __ , il s'agit d'un DataObjectGroup qui est rattaché à un ArchiveUnit virtuel de même nom (A valider)
  
    - Ce répertoire ne doit contenir que des fichiers . En cas de présence d'un répertoire, il s'agit d'une erreur bloquante lors du parsing de l'arborescence
    - Le nom du dataobjectgroup est le nom du répertoire auquel on retranche les __ de début et de fin
    - L'archive Unit Virtual a le champ DescriptionLevel à Item
	
  + Répertoire dans le cadre général
  
    - Le nom du répertoire est le champ Content/Title de l'archive unit
    - Les métadonnées de gestions seront une balise vide (<Management/>)
    - Dans cette première version, il n'y aura pas d'enrichissement des méta-données par des métadonnées spécifiques à chaque archive unit
    - L'archive Unit dans le cas général a le champ DescriptionLevel à RecordGrp
	
  + Répertoire qui n'est pas un dataobjetgroup et contenant des fichiers
  
    - il s'agit d'une Archive Unit ayant virtuellement par fichier présent 1 archive Unit qui contient lui-même 1 DataObjectGroup 
	- L'archive Unit Virtual a le champ DescriptionLevel à Item (A valider)
	- L'archive Unit Virtual a le champ transactedDate à Modification time du fichier (Non Implémenté)
	- Un fichier vide est ignoré en logguant la présence de ce fichier vide mais il ne s'agit que d'un warning (non bloquant) (A valider)
    - Un fichier caché (au sens attribut Windows) est ignoré (Non implémenté)	
* Dans l'arborescence, on peut trouver des fichiers :

  - Le nom manifest.json est un nom reservé pour ultérieurement et sera ignoré comme BinaryDataObject
  - L'algorithme utilisé pour pour le Message Digest est SHA-512
  - L'identification du format n'est pas effectué (balise vide <FormatIdentifier/>)

Example d'arborescence
----------------------

Arborescence sources
^^^^^^^^^^^^^^^^^^^^
:: 

  Répertoire : /A
  Fichier    : /A/a1
  Fichier    : /A/a2
  Répertoire : /A/B
  Fichier    : /A/B/b1
  Répertoire : /A/__C__
  Fichier    : /A/__C__/c1
  Fichier    : /A/__C__/c2
  Répertoire : /A/D
  Répertoire : /A/D/E

SEDA (sans les DataObject)
^^^^^^^^^^^^^^^^^^^^^^^^^^

::

  ArchiveUnit     : A (racine)
  ArchiveUnit     : a1 (père : A )
  DataObjectGroup : dog_a1 (père:  a1), contient le BinaryDataObject a1
  ArchiveUnit     : a2 (père : A )
  DataObjectGroup : dog_a2 (père:  a2), contient le BinaryDataObject a2
  ArchiveUnit     : B (père : A)
  ArchiveUnit     : b1 (père : B)
  DataObjectGroup : dog_b1 (père:  b1), contient le BinaryDataObject b1
  ArchiveUnit     : C (père : A)
  DataObjectGroup : dog_C (père:  C), contient les BinaryDataObject c1 et c2
  ArchiveUnit     : D (père : A)
  ArchiveUnit     : E (père : D)


Sortant de l'outil
------------------

Un fichier ZIP (pkZIP) avec les fichiers suivants : 
 * Un fichier SEDA généré qui est valide par rapport au schéma SEDA 2.0 (MENODA)
 
   + (A valider) L'encodage du fichier de sortie doit être en UTF-8
   + (Souhait) Les PO souhaitent avoir un affichage des archive Unit dans l'ordre du parcours en profondeur de l'arborescence
 * Un répertoire Content qui contient l'ensemble des BinaryDataObject décrits dans le fichier SEDA. Chaque fichier a pour nom son ID dans le bordereau Seda


Couverture du SEDA 
------------------

Dans le fichier SEDA, les Champs suivants sont gérés 
 * ArchiveUnit : Comment,MessageIdentifier,ArchivalAgreement,CodeListVersions,ArchivalAgency.Identifier,TransferringAgency.Identifier sont gérables par un fichier json fourni en paramètre
 * DataObjectPackage.BinaryDataObject 
 
   + DataObjectGroupId : généré programmatiquement
   + DataObjectVersion : fixé arbitrairement à DataObjectVersion0
   + Uri : Content/<ID du Binary DataObject>
   + MessageDigest : Fourni l'empreinte en SHA-512
   + Size : Fourni la taille du fichier
   + FormatIdentification : si le module Siegfried est activé, on positionne les 3 champs FormatLitteral,MimeType,FormatId
   + FileInfo : FileName et LastModified (mtime du fichier)
 * DataObjectPackage.Management : élément vide
 * DataObjectPackage.DescriptiveMetadata.ArchiveUnit.Content :
 
   + DescriptionLevel : File s'il y a un DOG comme fils , RecordGrp sinon
   + Title : Nom du fichier ou répertoire
   + Description : un champ de debug pour l'instant
   + TransactedDate : Pour les archiveUnit ayant un DOG, le mtime du dernier fichier chargé dans le DOG . Quelle est la bonne valeur quand il y a plus d'un fichier par DOG
  
Interface de l'outil
--------------------

Il s'agit d'un logiciel en ligne de commande qui prendra les arguments suivants : 

* Nom du répertoire dont on désire obtenir le bordereau SEDA associé . Ce répertoire permettra de générer le bloc DataObjectPackage
* Fichier paramètre contenant les élements globaux du SEDA (Coment, MessageIdentifier, ArchivalAgreement,CodeListVersions,ArchivalAgency,TransferringAgency)
* Chemin (relatif ou absolu) du fichier SEDA généré

Ecart au standard
---------------

* Modification du SEDA : La restriction XMLREFID a été supprimé pour  ArchiveUnitRefIdType et GroupRefIdType

Charges
-------
17 points
