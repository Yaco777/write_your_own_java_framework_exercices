Un injecteur eregistre pour chaque classe l'instance qui va être utilisé.
On peut utiliser le registerInstance / lookupInstance de InjectorRegistry.

-> registerProvider donne une instanc eà chaque appel, donc si on fait deux appels on a deux instances
-> registerProviderClass demande une classe

@Inject se met sur des constructeurs et des setteurs, seul ces méthodes sont appelé par les méthodes 

## Cast

Le cast (T) doit être évité, il vaut mieux faire un cast (il sera fait à l'exécution).

## Type paramétré 

On peut indiquer un type générique dans la signature, cela peut-être utile pour indiquer que les arguments sont du même type
On peut faire ? extends T pour avoir un sous-type de T (ou T lui même)

## Optional

Or else get est mieux que orElse puisque orElse va executer le code du supplier dans tout les cas
alors que or else get n'appel le supplier que si la condition est fausse

## Résumé

L'injection va vérifier qu'on a un seul constructeur avec @Inject, cela va vérifier le type de tous les arguments
Après avoir créé l'instance, on va appeler tous les setters qui possèdent un @Inject


