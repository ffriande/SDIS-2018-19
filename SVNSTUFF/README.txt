++++++++++++++++++++++++++++++++++
SCRIPTS FOR SDIS-2018-19 PROJECT 1
++++++++++++++++++++++++++++++++++

Our project offers the option to use scripts to facilitate the running and compiling process, these can be applied either on Linux or Windows operating systems.

=======
WINDOWS
=======
 
1. We suggest (as that was our testing environment) opening up to 4 different terminals (1 for the TestApp and the 3 for Peers).
2. On one of the terminals run the script build.bat (this script will compile and open the rmiregistry).
3. On the three other terminals, run the scripts runPeer1.bat, runPeer2.bat, runPeer3.bat (respectively), this will open a peer per terminal.
4. Issue whatever commands necessary in the designated TestApp terminal.
4.1. It is possible to use runClient-RESTORE.bat to issue a restore for the test file test1.png on peer p1.
4.2. It is possible to use runClient-BACKUP.bat to issue a backup of test1.png on p1 with replication degree 1.

Note: All calls to TestApp must be in the format: java TestApp <peer access point> <protocol in capital letters> <test file> (<rep degree>) 
The rep degree is optional.

=====
LINUX
=====

1. Open 5 terminals.
2. Run rmiregistry in one of the terminals.
3. Run the script peerLinux.sh for 3 of the terminals. This script will compile and also run 1 peer per terminal, with the following usage:
sh peerLinux.sh <peerId> <version> <accessPoint>
3.1 If nothing is specified, peerId will by default be 1, version will be 1.0 and accessPoint will be peer1.
4. Use the remaining terminal to issue the desired commands like so:
java TestApp <peer access point> <protocol in capital letters> <test file> (<rep degree>)?
