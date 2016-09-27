Prérequis 
=========

- Disposer d'au moins 1 Go de RAM sur son poste de travail
- Disposer de 50 Mo de disque disponible (+ espace nécessaire pour la génération des archives)
- Disposer d'un JRE 8 sur le poste de travail (testé avec la JRE Oracle sous Windows et JRE OpenJDK sous Linux)
- Sous Windows, ne pas avoir de processus écoutant sur le port 5138
  
  
Lancement
=========

Lancement sous windows 
----------------------

La version Windows inclut l'outil d'identification de format Siegfried avec une liste de signature

Lancer le script run_generator.bat avec 1 paramètre : Répertoire de base de l'arborescence que l'on désire scanner (ce doit être un répertoire et non un fichier seul). 

Ceci peut notamment être fait avec un drag-and-drop dans l'explorateur windows en faisant "glisser" un répertoire sur le fichier run_generator.bat

Sous Windows, le script inclut le démarrage de Siegfried (http://www.itforarchivists.com/siegfried) en mode serveur (port 5138) puis arrête Siegfried à la fin de l'exécution

Lancement sous Linux
---------------------

La version Linux a besoin comme pré-requis de l'outil d'identification de format Siegfried : 

  * Pour une distribution basée sur des packages deb en 64 bits , suivre la procédure https://github.com/richardlehane/siegfried#ubuntudebian-64-bit
  * Pour une distribution RPM, il n'y a pas pour l'instant de version compilée

Lors du 1er lancement, il faut récupérer le fichier de signature : ``sf -update`` (accès HTTP sans proxy sur Internet nécessaire)

Lors de chaque lancement : 

  * Lancer siegfried en mode serveur sur le port 5138 : ``sf -serve localhost:5138``
  * Lancer le script run_generator.sh avec 1 paramètre : Répertoire de base de l'arborescence que l'on désire scanner

Fichiers d'entrée
=================

Arborescence d'entrée
---------------------

Voir le fichier doc/Arborescence.rst pour les spécification de l'arborescence d'entrée

Fichiers de configuration
-------------------------

Dans le répertoire conf : 

  * logback.xml : fichier de configuration logback standard
  * ArchiveUnitTransfer.json : Metadonnées globales de l'ArchiveTransfer (Comment, MessageIdentifier, ArchivalAgreement, CodeListVersions, ArchivalAgency et TransferringAgency) 
  * playbook_BinaryDataObject.json : Définition du "workflow" des différentes étapes pour un BinaryDataObject

Voir le fichier doc/Configuration.rst pour plus de détails

Fichiers de sortie
==================

2 fichiers : 
 * Un fichier horodaté ZIP (pkZIP) avec les fichiers suivants (SIP-yyyyMMddHHmmss.zip) : 
   * Un fichier SEDA généré qui est valide par rapport au schéma SEDA 2.0 (MEDONA) : l'affichage des ArchiveUnit est dans l'ordre du parcours en profondeur de l'arborescence
   * Un répertoire Content qui contient l'ensemble des BinaryDataObject décrits dans le fichier SEDA. Chaque fichier a pour nom son ID dans le bordereau Seda
 * Un fichier avec la liste des fichiers rejetés (SIP-yyyyMMddHHmmss.rejected) 

Compilation & Packaging
=======================

Pour le packaging, il faut :

  * lancer la compilation des classes : ``mvn clean package``
  * se positionner dans scripts et lancer ``sh generate_package.sh`` . Le résultat est dans scripts/build

