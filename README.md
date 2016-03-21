# riemann-mbeans

A [riemann](http://riemann.io/) plugin for periodically collecting JMX stats from the JVM it's running in.

## Installation

After cloning the repo, you can build the plugin using [leiningen](/technomancy/leiningen)

```
lein uberjar
```

This will create a plugin jar named `mbeans-x.y.z-SNAPSHOT-standalone.jar` which you can include into your *java classpath*, *e.g.*:

```
java -cp /usr/lib/riemann/riemann.jar:/usr/lib/riemann/mbeans-0.1.1-SNAPSHOT-standalone-up.jar riemann.bin start /etc/riemann/riemann.config
```

On debian or redhat you could also add the classpath using the `EXTRA_CLASSPATH` variable available respectively in `/etc/default/riemann` or `/etc/sysconfig/riemann`.

## Synopsis

```clojure
(load-plugins)

(mbeans/instrumentation {:interval 10})
```

