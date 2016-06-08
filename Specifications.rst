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
  + Répertoire dont le nom commence par __ et termine par __ , il s'agit d'un DataObjectGroup qui est rattaché à un ArchiveUnit virtuel de même nom (Non Implémenté)
    = Ce répertoire ne doit contenir que des fichiers . En cas de présence d'un répertoire, il s'agit d'une erreur bloquante lors du parsing de l'arborescence
    = Le nom du dataobjectgroup est le nom du répertoire auquel on retranche les __ de début et de fin
	= L'archive Unit Virtual a le champ DescriptionLevel à Item
  + Répertoire dans le cadre général
    = Le nom du répertoire est le champ Content/Title de l'archive unit
    = Les métadonnées de gestions seront une balise vide (<Management/>)
    = Dans cette première version, il n'y aura pas d'enrichissement des méta-données par des métadonnées spécifiques à chaque archive unit
	= L'archive Unit dans le cas général a le champ DescriptionLevel à RecordGrp
  + Répertoire qui n'est pas un dataobjetgroup et contenant des fichiers
    = Il s'agit d'une Archive Unit ayant virtuellement 1 archive Unit qui contient lui-même 1 dataobjectgroup par fichier présent
	= (Non Implémenté) L'archive Unit Virtual a le champ DescriptionLevel à Item
	= (Non Implémenté) L'archive Unit Virtual a le champ transactedDate à Modification time du fichier 
	= (Non Implémenté) Un fichier vide est ignoré en logguant la présence de ce fichier vide mais il ne s'agit que d'un warning (non bloquant)
* Dans l'arborescence, on peut trouver des fichiers :
  + Le nom manifest.json est un nom reservé pour ultérieurement et sera ignoré comme BinaryDataObject
  + L'algorithme utilisé pour pour le Message Digest est SHA-512
  + L'identification du format n'est pas effectué (balise vide <FormatIdentifier/>)

Sortant de l'outil
------------------

Un fichier SEDA généré qui est valide par rapport au schéma SEDA 2.0 (MENODA)
(Non implémenté) L'encodage du fichier de sortie doit être en UTF-8
(Souhait) Les PO souhaitent avoir un affichage des archive Unit dans l'ordre du parcours en profondeur de l'arborescence

Interface de l'outil
--------------------

Il s'agit d'un logiciel en ligne de commande qui prendra les arguments suivants : 
* Nom du répertoire dont on désire obtenir le bordereau SEDA associé . Ce répertoire permettra de générer le bloc DataObjectPackage
* Fichier paramètre contenant les élements globaux du SEDA (Coment, MessageIdentifier, ArchivalAgreement,CodeListVersions,ArchivalAgency,TransferringAgency)
* Chemin (relatif ou absolu) du fichier SEDA généré

Dette technique
---------------

* Gestion des Exceptions
* Gestion des REFID pour ArchiveUnitRefIdType et GroupRefIdType . La restriction XMLREFID a été supprimé pour ces 2 champs 
