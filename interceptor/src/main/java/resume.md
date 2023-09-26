
Un intercepteur se place entre l'appel des méthodes et l'interface. Il permet d'executer du code
avant l'appel de la méthode et après. Cela peut-être utile pour check que les arguments soient non null.

## Proxy
Un proxy se trouve devant l'interface et va interecepter tous les appels pour ensuite appeler l'advice.
Ainsi, pour chaque méthode de l'interface cela va executer le code du proxy.

