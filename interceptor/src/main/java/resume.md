
Un intercepteur se place entre l'appel des méthodes et l'interface. Il permet d'executer du code
avant l'appel de la méthode et après. Cela peut-être utile pour check que les arguments soient non null.

## Proxy
Un proxy se trouve devant l'interface et va interecepter tous les appels pour ensuite appeler l'advice ou l'intercepteur.
Ainsi, pour chaque méthode de l'interface cela va executer le code du proxy.

## Advices
Code lancé avant et après l'appel à une fonction. Le problème c'est qu'il n'est pas possible de savoir si un null est un retour de fonction ou une exception.

## Les intercepteurs

Les appels aux invocations se font dans l'ordre inverse. On créer l'invocation qui fait l'appel à la méthode, puis le précédent interceptor va appeler la prochaine invocation.
Cela se fait dans l'ordre inverse : on construit de la fin et on recule.

L'avantage c'est que si on a un null en valeur de retour on le saura direct (contrairement aux advices)

