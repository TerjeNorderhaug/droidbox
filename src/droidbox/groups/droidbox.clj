(ns droidbox.groups.droidbox
    "Node defintions for droidbox"
    (:require
     [pallet.api :refer [group-spec server-spec node-spec plan-fn converge lift]]
     [pallet.crate.automated-admin-user :refer [automated-admin-user]]
     [pallet.actions :refer [package package-manager remote-directory remote-file directory exec-script setup-node]]
     [pallet.action :refer [with-action-options]]
     [pallet.script.lib :as lib :refer [config-root file user-home]]
     [pallet.stevedore :refer [script checked-script fragment]]
     [pallet.core.user :refer [*admin-user*]]
     [pallet.crate :refer [defplan admin-user]]
     [pallet.crate.java :as java]
     [pallet.crate.lein :refer [lein leiningen]]
     [pallet.crate.environment :refer [system-environment]]))

(def default-node-spec
  (node-spec
   :image {:image-id :ubuntu-14.04}
   :hardware {:min-cores 1 :min-ram 1024}))

(def
  ^{:doc "Defines the type of node droidbox will run on"}
  base-server
  (server-spec
   :phases
   {:bootstrap (plan-fn (automated-admin-user))}))

(def 
  ^{:doc "Components missing for various crates"}
  missing-prerequisites  
  (server-spec
    :phases
      {:configure
          (plan-fn 
            (package-manager :update)
            (package "wget") ; required for remote-file in leiningen crate
            (package "unzip") ; general use
            (package "python-software-properties")
            (package "software-properties-common")
            ;(pallet.actions/exec-script
              ; provides add-apt-repository required to install java
              ;(lib/install-package "python-software-properties")
              ;(lib/install-package "software-properties-common")
;              )
) }))

(def java-server
  (java/server-spec
    {:vendor :oracle
    ; :components #{:jdk}
    :version "7"}))

(defplan lein-droid-deploy
  []
  (setup-node)
  (with-action-options {:script-prefix :no-sudo}
    (lein "self-install")
    (remote-file (fragment (file ".lein" "profiles.clj")) 
                 :content (pr-str {:user {:plugins '[ [lein-droid "0.2.3"] ] :android {:sdk-path "/opt/android/sdk"}}}))))

(def lein-installation
  (server-spec
    :extends [(leiningen {})]
    :phases
      {:configure (plan-fn
                   (system-environment "lein-env" {"PATH" "/usr/local/bin/:$PATH"})
                   (lein-droid-deploy)) }))

(defplan adt-dependencies
  []
  (package "lib32stdc++6")
  (package "lib32z1"))

(defplan adt-install
  []
  (remote-directory "/opt"
                    :unpack :unzip
                    :mode "755"
                    :url "http://dl.google.com/android/adt/adt-bundle-linux-x86_64-20140702.zip")
  (exec-script (lib/mv "/opt/adt-bundle-linux-x86_64-20140702" "/opt/android" :force true))
  (exec-script (lib/cp "/opt/android/sdk/build-tools/android-4.4W/zipalign" "/opt/android/sdk/tools/zipalign")) ; for older build tools
  (directory "/opt/android/sdk" :mode "755" :recursive true)
  (pallet.actions/exec-script
   (lib/chmod "755" "/opt/android/sdk/tools/*")
   (lib/chmod "755" "/opt/android/sdk/platform-tools/*"))
  (adt-dependencies)
  (system-environment "android-tools-env" {"PATH" "/opt/android/sdk/tools/:/opt/android/sdk/platform-tools/:$PATH"})) 

(def
  ^{:doc "Define a server spec for droidbox"}
  droidbox-server
  (server-spec
   :extends [lein-installation]
   :phases
   {:configure (plan-fn
                 (adt-install)
                 (package-manager :update)
                 (package "maven")) }))

(def
  ^{:doc "Defines a group spec that can be passed to converge or lift."}
  droidbox
  (group-spec
   "droidbox"
   :extends [base-server missing-prerequisites java-server droidbox-server]
   :node-spec default-node-spec))





