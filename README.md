# riemann-mbeans

A [riemann](http://riemann.io/) plugin for periodically collecting JMX stats from the JVM it's running in.

## Installation

After cloning the repo, you can build the plugin using [leiningen](/technomancy/leiningen)

```
lein uberjar
```

This will create a plugin jar named `mbeans-x.y.z-SNAPSHOT-standalone.jar` which you can include into your *java classpath*, *e.g.*:

```
java -cp /usr/lib/riemann/riemann.jar:/usr/lib/riemann/mbeans-0.2.1-SNAPSHOT-standalone-up.jar riemann.bin start /etc/riemann/riemann.config
```

On debian or redhat you could also add the classpath using the `EXTRA_CLASSPATH` variable available respectively in `/etc/default/riemann` or `/etc/sysconfig/riemann`.

## Synopsis

### Defaults

```clojure
(load-plugins)

(mbeans/instrumentation)
```

### Custom beans

```clojure
(load-plugins)
(let [beans [{:mbean "java.lang:type=Memory" :property :HeapMemoryUsage :attribute :used}
             {:mbean "java.lang:type=Runtime" :property :Uptime}]]
	(mbeans/instrumentation {:interval 10 :mbeans beans}))
```

### Custom beans with custom service name

```clojure
(load-plugins)
(mbeans/instrumentation {:interval 10 :mbeans [{:mbean "java.lang:type=Runtime" :property :Uptime :service "the jvm's uptime"}]})
```

## Usage

### `mbeans/instrumentation opts`

This function will add a service to the riemann core that will retrieve JMX mbeans on a periodic fashion.
The map `opts` should contain two keys:

* `interval` periodicity in seconds (defaults to 10)
* `mbeans` sequence of hash maps describing the beans to collect (See [clojure/java.jmx](https://github.com/clojure/java.jmx)). Every element of `mbeans` should contain the following keys:
  * `mbean` the name of the bean
  * `property` the bean's property to collect
  * `attribute` the property's attribute to collect (optional if the property is scalar)
  * `service` the name of the riemann event's service. This defaults to the concatenation of the three previous values.

The service should behave as expected with respect to a configuration reload!

### Caveats

* Not sure if there are any nested types or tables. If there are, their retrieval will probably require an API change.
* If one bean fails, nothing will be reported

