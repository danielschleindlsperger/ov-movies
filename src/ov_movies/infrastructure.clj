(ns ov-movies.infrastructure
  (:require
    [clojure.java.io :as io]
    [cognitect.aws.client.api :as aws]))

(defn sleep-until
  "Sleeps for `n` seconds. Then applies `f` without arguments.
  If `f` returns a false value, rerun."
  [predicate n]
  (Thread/sleep (or n 1000))
  (when-not (predicate) (sleep-until predicate n)))

(def cf (aws/client {:api :cloudformation}))
(aws/validate-requests cf true)

(def resource
  "Load a file from the 'resources' folder. Returns a java.io.file."
  (comp io/file io/resource))

(defn load-template [] (slurp (resource "cloudformation.yml")))

(defn stack-exists? [stack-name] true)

(defn stack-status [stack-name]
  (-> (aws/invoke cf {:op :DescribeStacks :request {:StackName stack-name}})
      :Stacks (get 0) :StackStatus))

(defn create-or-update-stack [stack-name template-body]
  (let [op (if (stack-exists? stack-name) :UpdateStack :CreateStack)]
    (aws/invoke cf {:op op :request {:StackName stack-name :TemplateBody template-body}})
    (println (stack-status stack-name))))

(defn -main [] (create-or-update-stack "clj-test-stack" (load-template)))

;:CreateStack
;:UpdateStack