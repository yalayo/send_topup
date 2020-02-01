(ns topup.cdk
  (:require [clojure.edn :as edn]
            [clojure.java.io :as io]
            [stedi.cdk.alpha :as cdk]
            [uberdeps.api :as uberdeps]))

(cdk/import [[App Construct Duration Stack] :from "@aws-cdk/core"]
            [[Code Function Runtime Tracing] :from "@aws-cdk/aws-lambda"])

(defn- clean
  []
  (let [f (io/file "classes")]
    (when (.exists f)
      (->> f
           (file-seq)
           (reverse)
           (map io/delete-file)
           (dorun)))))

(def code
  (let [jarpath "target/app.jar"
        deps    (edn/read-string (slurp "deps.edn"))]
    (with-out-str
      (clean)
      (io/make-parents "classes/.")
      (io/make-parents jarpath)
      (compile 'depswatch.lambada)
      (uberdeps/package deps jarpath {:aliases [:classes]}))
    (Code/fromAsset jarpath)))

(defn LambadaFunction
  [scope id props]
  (Function scope
            id
            (merge {:code       code
                    :memorySize 2048
                    :runtime    (:JAVA_8 Runtime)
                    :tracing    Tracing/ACTIVE}
                   props)))

(defn AppStack
  [scope id]
  (let [stack         (Stack scope id)]
    (stack))

(def app (App))

(AppStack app "depswatch-dev")
(AppStack app "depswatch-prod")
