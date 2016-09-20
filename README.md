# Orienteering
Orienteering App based on the Gluon Framework using FireBase as CloudBackend.

To be able to use the app, you first have to create an user. 
Every user can create cities, tasks and missions. 

A city is always accessible by all users, but can only be edited by its owner.

Tasks and missions can have be public or private. Private ones are only visible to its  owner,  while public ones are visible to everyone.
Private tasks can be made visible to a user other than their owner, when they are added to a public mission by their owner.

When you start a mission, the first task is select and the corresponding gps location is shown on the map. When the location is reached, the task is marked complete, and the next task is selected.

If you can't find the location, there is the  possibility to skip the current task, with the skip icon in the titlebar (though the mission is marked incomplete and you won't be able to store the missionstat).

Next to the skip icon there is the scan icon, which allows to scan a barcode  to mark the task complete.

When a mission is accomplished, the missionstats (start, end, duration, distance) can be saved and  in the case of a public mission compared to other users. 

Most of the listviews provide some additional behaviour when you swipe a listcell left or right.
Swipe right e.g. deletes the listcell item (city, mission, task) or removes a task from a mission.
Swipe left e.g. selects a city as the default city, a mission as the active mission, or adds a task to a mission.

The app is a showcase for the Gluon Framework and until now not intended to be published to an App Store, because there is still a lot of work to do, e.g. Authentication,  Authorisation and Synchronisation. 

**WARNING: To be able to use the functions of the app, the User must have an Internet connection, which may be subjected to costs. Use on your own risk**





 


