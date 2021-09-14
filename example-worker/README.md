# Example Worker

This example worker will be used to briefly highlight the different pieces required to use Clojurescript with Cloudflare workers.
It is somewhat opinionated (e.g. clojurescript build tool) but you should be able to swap these out as you go.

As there are quite a few configuration files required I recommend using this directory as a template.

## Prerequisites

- [leiningen](https://leiningen.org/)
- [wrangler](https://developers.cloudflare.com/workers/cli-wrangler/install-update)
- [calva][https://calva.io/getting-started/]
## Bootstrap the project

We'll start things of by creating the `project.clj` to define the structure of the Clojurescript project and how it should be build.

```
(defproject example-worker "0.1.0-SNAPSHOT"
  :description "Example Clojurescript Cloudflare worker"
  :plugins [[lein-cljsbuild "1.1.8"]]
  :dependencies [[org.clojure/clojure "1.10.3"]
                 [org.clojure/clojurescript "1.10.339"]
                 [com.github.sauercrowd/clojureworker "0.0.1"]]
  :source-paths ["src"]
  :hooks [leiningen.cljsbuild]
  :cljsbuild {
    :builds {:production
               {:source-paths ["src"]
                :compiler {:output-to "target/worker.js"
                           :optimizations :advanced}}}})
```

This project uses lein-cljsbuild to allow leiningen to build Clojurescript projects. We also define that the code is defined in the `src/` directory
and that everything should be bundled up into the `target/worker.js` file, wich will be then passed to wrangler later on.
We're using advanced optimizations but we shouldn't worry about that too much yet.

## Create the worker

Next we'll get to the code, and luckily there's not a lot required.
Create the file `src/new_worker/core.cljs` and it's respective directories.
Now paste in

```
(ns new-worker.core
  (:require [clojureworker.core :as clfl]))

(clfl/worker
  (clfl/route "GET" "/worker" "hello from my worker"))
```

That done we can now compile our worker into `target/worker.js` using

```
lein compile
```

## Deploying with wrangler

Next create a simple `package.json` outlining where te worker code can be found:

```
{
    "main": "target/worker.js"
}
```

And now the wrangler configuration, `wrangler.toml`. To actually deploy it is it required to fill in the details as usually, but to run the preview we can leave the majority of fields empty:


```
name = "test-worker"
type = 'webpack'
account_id = ''
route = ''
zone_id = ''
usage_model = ''
workers_dev = true
target_type = "webpack"
```

To now preview the worker run

```
$ wrangler preview -u https://example.com/worker --headless

up to date, audited 1 package in 594ms

found 0 vulnerabilities
⚠️  Your configuration file is missing the following fields: ["account_id"]
⚠️  Falling back to unauthenticated preview.
👷  Your Worker responded with: hello from my worker
```

That's all!

Check out the [example clojure worker](src/my_worker/core.cljs) to see how promises and maps are returned, how you can provide a custom handler and how to use `simulate-worker` to ease the development process.


Note: `wrangler_tests.sh` is part of the integration test pipeline, validating that the example worker can run and returns the expected responses.
