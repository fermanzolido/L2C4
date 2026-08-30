Third party libraries only.

GameServer.jar and LoginServer.jar are NOT kept here. The build produces them and
places them in this folder of the assembled distribution, which is what the
launchers in ../game and ../login point at.

They used to be committed here, six months stale, and that was a trap in two ways.
Anyone assembling a pack by copying this folder over the build output silently
replaced freshly compiled code with the old jars and ran a server that had none of
the source changes, while the data and configuration around it were current. And
javac had a stale copy of this project's own classes on its classpath.

Running dist/game/GameServer.vbs from a clone now fails with a missing jar instead
of quietly running old code. Build first.
