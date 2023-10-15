Hibernate -> on track les objets, lorsqu'il y a une modification on enregistre cette modification. Cela utilise à la fois les getters et les setters
Il permet donc de savoir "tel objet à changé" mais on ne sait pas la quelle.

Transaction pessimiste -> on garde un lock sur une table et personne peut y toucher, il y a donc bcp d'attentes.

Dans la classe on a besoin d'un constructeur vide pour pouvoir crééer l'objet et ensuite appeler les setters pour bien créer l'objet.
@Id -> clé primaire
@GeneratedValue -> génère automatiquement l'id, on utilise un Double et pas un String
Comme on manipule des beans, il faut faire equals.

Repository -> objet java qui représente l'ensemble des querry qu'on peut faire
Les arguments du reposityroy sont <Bean de la classe, type de l'id>

Merge se base sur la clé primaire. Si ce n'est pas présent cela fait un INSERT, sinon cela fait un UPDATE




