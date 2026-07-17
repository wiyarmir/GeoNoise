# GeoNoise
Bachelor thesis for the BEng in Telecommunications at Universidad de Málaga

Topic: Android application for ambient noise measuring and geolocalised representation

# Building
Build with the Gradle wrapper: `./gradlew assembleDebug`.

The Google Maps API key is injected at build time, so it is never committed. Add
your key to the repo-root `local.properties` (git-ignored) before building:

```
MAPS_API_KEY=AIza...
```

Alternatively export it as a `MAPS_API_KEY` environment variable (e.g. on CI). If
neither is set the app still builds, but the map won't render.

# Author
[Guillermo Orellana Ruiz](https://profiles.udacity.com/u/guillermoorellana)
# Director
[Enrique Márquez Segura](http://www.ic.uma.es/contenidos/ficha_personal.action?id=783)
# Documentation
Full documentation is available together with LaTeX source (in spanish) under `/doc/` directory.
# Screenshots
![1](https://raw.githubusercontent.com/wiyarmir/GeoNoise/master/screenshots/device-2014-11-02-184441.png)
![2](https://raw.githubusercontent.com/wiyarmir/GeoNoise/master/doc/graphs/roadmapped.png) 
![3](https://raw.githubusercontent.com/wiyarmir/GeoNoise/master/doc/graphs/parkmapped.png)
