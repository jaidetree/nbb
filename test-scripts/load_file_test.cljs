(ns load-file-test
  (:require [nbb.core :refer [load-file *file*]]))

(def f *file*)
(.then (load-file "test-scripts/loaded_by_load_file_test.cljs")
       (fn [m]
         (assoc m :load-file-test-file-dyn-var f)))
