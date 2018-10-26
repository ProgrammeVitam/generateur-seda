Architecture générale
=====================

Le générateur SEDA est constitué des différents modules suivants :

* Point d'entrée principal du générateur SEDA :

  + ``scanner`` : il s'agit du point d'entrée qui gére le parcours d'une arborescence de fichiers. Il contient notamment les régles spécifiques de l'arborescence de fichiers (formalisme des répertoires des DOG, exclusions de fichiers etc.)

* Bibliothèques associées :
 
  + ``scheduler`` : ce module est un ordonnanceur générique de tâches
  + ``seda`` : ce module contient :

    - la définition du schéma XML du SEDA 2.1
    - les régles de gestions des objets composant le SEDA
    - les modules pour le scheduler nécessaires à la gestion des BinaryDataObject

* Dans le sous-module ``testing-modules``, les projets extras utilisés en interne Vitam pour la recette (donc sans engagement de Vitam) :

  + ``dag-generator`` : generation d'un SIP ayant un graphe acyclique orienté (DAG) fortement connecté
  + ``binary-generator`` : génération d'un SIP "en rateau" complètement généré (y compris les binary objects) ; contient des modules additionnels permettant la génération de binary objects
  + ``seda-invalid`` : ce module est destiné à des fins de recette uniquement et n'a pas à être inclus pour un usage opérationnel. Il fournit des modules pour le scheduler pour le traitement des BinaryDataObject permettant de fournir un SEDA fonctionnellement erronné (ex : mauvaise taille ou mauvaise empreinte d'un BinaryDataObject)

Le générateur SEDA possède deux packaging finaux :

* ``dist`` : réalise le packaging du générateur à destination des utilisateurs métier ;
* ``testing-dist`` : réalise le packaging du générateur à destination des testeurs.

.. Ce document est distribué sous les termes de la licence Creative Commons Attribution - Partage dans les Mêmes Conditions 3.0 France (CC BY-SA 3.0 FR)
