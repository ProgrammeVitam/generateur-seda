Compilation & Packaging
-----------------------

Pour le packaging, il faut :

  * lancer la compilation des classes : ``mvn clean package``
  * se positionner dans scripts et lancer ``sh generate_package.sh`` . Le résultat est dans scripts/build

Lancement
---------
Lancer le script run_generator avec 3 paramètres : 

  * Répertoire de base de l'arborescence que l'on désire scanner 
  * Le fichier json des paramètres globoaux
  * Le chemin du fichier ZIP à produire
