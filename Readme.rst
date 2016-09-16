Compilation & Packaging
=======================

Pour le packaging, il faut :

  * lancer la compilation des classes : ``mvn clean package``
  * se positionner dans scripts et lancer ``sh generate_package.sh`` . Le résultat est dans scripts/build


Lancement
=========

Lancement sous windows 
----------------------

La version Windows inclut l'outil d'identification de format Siegfried avec une liste de signature

Lancer le script run_generator.bat avec 1 paramètre : Répertoire de base de l'arborescence que l'on désire scanner 

Sous Windows, le script inclut le démarrage de Siegfried en mode serveur (port 5138) puis arrête Siegfried à la fin de l'exécution

Lancement sous Linux
---------------------

La version Linux a besoin comme pré-requis l'outil d'identification de format Siegfried : 

  * Pour une distribution basé sur des packages deb en 64 bits , suivre la procédure https://github.com/richardlehane/siegfried#ubuntudebian-64-bit
  * Pour une distribution RPM, il n'y a pas pour l'instant de version compilée

Lors du 1er lancement, il faut récupérer le fichier de signature : ``sf -update`` (accès HTTP sans proxy sur Internet nécessaire )

Lors de chaque lancement : 

  * Lancer siegfried en mode serveur sur le port 5138 : ``sf -serve localhost:5138``
  * Lancer le script run_generator.sh avec 1 paramètre : Répertoire de base de l'arborescence que l'on désire scanner

Fichiers
========

Fichiers d'entrée
=================

Arborescence d'entrée
---------------------

Voir le fichier Specifications.rst pour les spécification de l'arborescence d'entrée

Fichiers de configuration
-------------------------

Dans le répertoire conf : 

  * generator.properties : 
  * logback.xml : fichier de configuration logback standard
  * metadata.json : Metadonnées globales de l'archive Transfer (Coment,MessageIdentifier,ArchivalAgreement,CodeListVersions,ArchivalAgency et TransferringAgency) 
  * playbook_BinaryDataObject.json : Définition du "workflow" des différentes étapes pour un BinaryDataObject

Fichiers de sortie
==================

2 fichiers : 

  * SIP-YYYYMMDDHHMMSS.zip : Bordereau SIP contenant les fichiers binaires et le fichier 
  * SIP-YYYYMMDDHHMMSS.rejected : liste des fichiers rejetés de l'arborescence d'entrée
