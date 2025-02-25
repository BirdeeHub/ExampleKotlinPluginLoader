I learned to use a profile better, and also changed a couple more things, 

and now it unloads everything except for classloaders that held JFrames that contain components.

There are no longer any active instances other than the loader with the mentioned JFrame itself. 

It closes the other stuff, including loaders that dont create frames with swing components with events in them.

I.E. it will clear an empty JFrame and its classloader, but if you put stuff inside it, it chokes up the unload.
(even if the plugin does frame.removeAll() and frame.dispose() before closing)

Profiler says its some sort of assertion lock but I have no idea how to access it. 

Ive tried clearing assertion status on the classloader on close and a few other things related to cleaning up within the plugin itself

**HELP** Im pretty stuck at this point. I think its due to swing and the EDT somehow, but I am having trouble finding info...

I just want it to release the plugin jar file so that you can edit it without closing the program...

If the ONLY reason is Swing, then I guess it is ok? Im making this as an extension to another program eventually that has functions to manage swing panels. 

But I thought I figured out everything that wasn't swing twice, and I still improved it further, so now I am not sure.

So I either need confirmation that the only remaining issue with unload is swing/EDT, or a suggestion as to how to remove this stupid assertion lock that I did not ask for.
