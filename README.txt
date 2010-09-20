
Prerequisites
-------------

The server module needs to be deployed on a running jboss instance, 
along with a process engine that provides an implementation of the integration layer.
See http://www.jboss.org/community/wiki/BPMConsoleReference for further explanations.

Build console
--------------------------

But the basics steps to get going are as follows:

1) Build the top level module

	mvn clean install

2) Make sure both process engine and the server module are installed on JBoss AS instance
	
	http://www.jboss.org/community/wiki/BPMConsoleReference

3) Boot the AS and start the gwt console in hosted mode

	See gui/war/README.txt for further information 


Troubleshooting
---------------

Please post any questions to the gwt-console developer forum.
(http://www.jboss.org/index.html?module=bb&op=viewforum&f=295)

Have fun.

