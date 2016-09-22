Architecture générale
=====================


Le générateur SEDA est constitué des différents modules suivants : 

 * scanner : il s'agit du point d'entrée qui gére le parcours d'une arborescence de fichier. Il contient notamment les régles spécifiques de l'arborescence de fichier (Formalisme des répertoire des DOG, exclusions de fichiers etc)
 * scheduler : ce module est un ordonnanceur générique de tâches 
 * seda : ce module contient 

   + la définition du schéma XML du SEDA 2.0
   + Les régles de gestions des objets composants le SEDA 
   + les modules pour le scheduler nécessaires à la gestion des BinaryDataObject

 * seda-invalid : ce module est à des fin de recette et n'a pas à être livré pour un usage opérationnel. Il fournit des modules pour le scheduler pour le traitement des BinaryDataObject permettant de fournir un SEDA fonctionnelement erronné (ex : mauvaise taille ou mauvaise empreinte d'un BinaryDataObject)

