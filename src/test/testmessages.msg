


(PROXY
 :X-comment "This message tests the case where the value of an ACLMessage  parameter is a number"
 :reply-with 99
)

(PROXY :reply-with +99)
(PROXY :reply-with -99)
(PROXY :reply-with +.99E-23)




(REQUEST
 :sender  ( agent-identifier  :name da0@FBELLIF:1099/JADE)
 :receiver  (set ( agent-identifier  :name df@FBELLIF:1099/JADE) )
 :content  "((action (Agent-Identifier :name DF@IOR:001122)
            (REGISTER 
             (Df-agent-description
             :name (Agent-Identifier :name x2@ior:002233)
             :services (set
               (Service-Description
                :name Service1
                :type TestType
                :ontologies (set Os1 Os2 Os3)
                :languages (set l1 l2 l3)
                :protocols (set Ps1 Ps2 Ps3)
                :ownership CSELT
                :properties (set (Property :name X1 :value V1) 
          			 (Property :name X3 :value 12) 
          			 (Property :name X4 :value -12.23) 
          			 (Property :name X5 :value 20001211T100025100A)
          			 (Property :name X6 :value (Agent-Identifier :name x))
          			 (Property :name X7 :value (Service-Description :name x))
                                 (Property :name X2 :value V2) )))
             :protocols (set FIPA-Request FIPA-Contract-Net)
             :ontologies (set FIPA-Agent-Management Trading)
             :languages (set FIPA-SL0)
          ))))" 
 :language  fipa-sl0
 :ontology  fipa-agent-management
 :protocol  fipa-request
 :X-comment "This message uses singular names for protocol, ontology, language slots in order to test FIPA2000 compliance."
)

(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content  "(  (action (Agent-Identifier :name \"action\" )
             (SEARCH
              (Df-agent-description
              :name (Agent-Identifier :name \"strng with \\"  and blanks\"))
              (search-constraints :max-depth 20 :max-results 10 )
             ))
           )"
 :X-comment "Here I want to test the String management of the SL0 parser "
)

(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "( (result (action (Agent-Identifier :name x@001122)
             (SEARCH
              (Df-agent-description
              :name (Agent-Identifier :name x@ior:002233))
              (search-constraints :max-depth 20 :max-results 10 )
             ))
             (set (DF-Agent-Description
                    :name (Agent-Identifier :name x@ior:002233))
                  (DF-Agent-Description :name (Agent-Identifier :name x2)))
           ))"
 :X-comment "Result of a search with the DF"
)

(REQUEST
 :language FIPA-SL0
 :ontology jade-agent-management
 :content "( (action (Agent-Identifier :name AMS)
              (kill-container 
               :container (Container-ID :name container-1)
               :password PWD    
             )))  "
 :X-comment "Some sample JADE agent management messages"
)

(REQUEST
 :language FIPA-SL0
 :ontology jade-agent-management
 :content "( (action (Agent-Identifier :name AMS)
              (create-agent
               :agent-name a-1
               :class-name examples.ontology.TestAgent
               :container (Container-ID :name c2)
               :password PWD    
             )))  "
 :X-comment "Some sample JADE agent management messages"
)


(INFORM
 :language FIPA-SL0
 :ontology jade-agent-management
 :content "( (done (action (Agent-Identifier :name AMS)
              (kill-agent
               :agent (Agent-Identifier :name a-1)
               :password PWD    
             )))  )"
 :X-comment "Some sample JADE agent management messages"
)


(INFORM
 :language FIPA-SL0
 :ontology jade-agent-management
 :content "( (done (action (Agent-Identifier :name AMS)
              (sniff-on
               :sniffer (Agent-Identifier :name a-1)
               :sniffed-agents (sequence (Agent-Identifier :name a-1))
               :password PWD    
             )))  )"
 :X-comment "Some sample JADE agent management messages"
)

(INFORM
 :language FIPA-SL0
 :ontology jade-agent-management
 :content "( (done (action (Agent-Identifier :name AMS)
              (sniff-off
               :sniffer (Agent-Identifier :name a-1)
               :sniffed-agents (sequence (Agent-Identifier :name a-1))
               :password PWD    
             ))))  "
 :X-comment "Some sample JADE agent management messages"
)


(SUBSCRIBE
 :language FIPA-SL0
 :ontology jade-introspection
 :content "( (occurred (Event-Record :what (added-Container :container (Container-ID :name c1) )))
            (occurred (Event-Record :what (removed-Container :container (Container-ID :name c1 ) )))
            (occurred (Event-Record :what (Born-Agent :where (Container-ID :name c2) :agent (Agent-identifier :name c1) ) ))
            (occurred (Event-Record :what (Dead-Agent :where (Container-ID :name c2) :agent (Agent-Identifier :name c1) ) ))
            (occurred (Event-Record :what (Moved-Agent :from (Container-ID :name c1) :to (Container-ID :name c2) :agent (Agent-Identifier :name pippo) ) ))
            true
            FALSE
          )"
)


(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "(( result (action (Agent-Identifier :name DF@IOR:001122)
                            (GET-DESCRIPTION))
           (AP-Description 
             :name JADE
             :dynamic true
             :mobility FALsE
             :transport-profile 
              (AP-Transport-Description
               :available-mtps (set
                (mtp-description :profile alpha :mtp-name IIOP
                 :addresses (sequence iiop://cselt.it:50/acc)))))
          ))"
 :X-comment "Inform of the result of the action"
)


(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((action (Agent-Identifier :name x@001122)
            (SEARCH
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233))
             (search-constraints )
            )))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)


(FAILURE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "( already-registered
            not-registered
            (internal-error \"Nothing was possibile\")
 )"
 :X-comment "a bit of failure exceptions are here. Notice that a number is not a String and therefore you must quote it."
)


(REQUEST
 :language FIPA-SL0
 :ontology jade-mobility-ontology
 :content "((action (Agent-Identifier :name x@001122)
            (query-platform-locations)))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)

(INFORM
 :language FIPA-SL0
 :ontology jade-mobility-ontology
 :content "( (RESULT (action (Agent-Identifier :name x@001122)
                            (query-platform-locations))
                      (SEQUENCE (Container-ID 
			                           :name Container1 
			                           :protocol RMI
                                 :address rmi://p.cselt.it:50)	
                                (Container-ID
			                           :name Container2 
			                           :protocol RMI2
                                 :address rmi2://p.cselt.it:50)
        ))))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)


(REQUEST
 :language FIPA-SL0
 :ontology jade-mobility-ontology
 :content "((action (Agent-Identifier :name x@001122)
            (move-agent
             (Mobile-Agent-Description
              :name (Agent-Identifier :name x@ior:002233)
              :destination (Container-ID
			    :name Container1 
			    :protocol RMI
                            :address rmi://p.cselt.it:50)
              :agent-profile (Mobile-Agent-Profile
                              :system (Mobile-Agent-System
                                       :name XXX
                                       :major-version 5
                                       :minor-version 0
                                       :dependencies \"A B C\") 
                              :language (Mobile-Agent-Language
                                         :name YYY
                                         :major-version 6
                                         :minor-version 1
                                         :dependencies \"E F G\")  
                              :os (Mobile-Agent-OS
                                         :name ZZZ
                                         :major-version 7
                                         :minor-version 2
                                         :dependencies \"E F G H\") ) 
              :agent-version JADE2.0
              :signature \"This is a String\"
            ))))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)


(INFORM
 :language FIPA-SL0
 :ontology jade-mobility-ontology
 :content "((done (action (Agent-Identifier :name x@001122)
                         (clone-agent
             (Mobile-Agent-Description
              :name (Agent-Identifier :name x@ior:002233)
              :destination (Container-ID
			    :name Container1 
			    :protocol RMI
                            :address rmi://p.cselt.it:50)
            ) NewName ))))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)

(REQUEST
 :language FIPA-SL0
 :ontology jade-mobility-ontology
 :content "((action (Agent-Identifier :name x@001122)
            (clone-agent
             (Mobile-Agent-Description
              :name (Agent-Identifier :name x@ior:002233)
              :destination (Container-ID
			    :name Container1 
			    :protocol RMI
                            :address rmi://p.cselt.it:50)
            ) NEWName )))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)


(INFORM
 :language FIPA-SL0
 :ontology jade-mobility-ontology
 :content "( (result (action (Agent-Identifier :name x@001122)
                            (where-is-agent (Agent-Identifier :name Giovanni)))
             (Container-ID
			    :name Container1 
			    :protocol RMI
                            :address rmi://p.cselt.it:50)
            ))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)




(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((action (Agent-Identifier :name x@001122)
            (SEARCH
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233))
             (search-constraints )
            )))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)


(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((action (Agent-Identifier :name x@001122)
            (SEARCH
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233))
             (search-constraints :max-depth 20 :max-results 10 )
            )))"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)



(FAILURE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "( already-registered
            not-registered
            (internal-error \"Nothing was possibile\")
 )"
 :X-comment "a bit of failure exceptions are here. Notice that a number is not a String and therefore you must quote it."
)


(REFUSE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "( unauthorised
            (unsupported-function PLUS) 
            (missing-parameter ACTION pippo)
            (unexpected-parameter Agent-Identifier x-pippo)
            (missing-parameter Agent-Identifier name)
            (unexpected-parameter Agent-Identifier X-userdefined)
            (unrecognised-parameter-value Agent-Identifier \"20\")
 )"
 :X-comment "a bit of refuse exceptions are here. Notice that a number is not a String and therefore you must quote it."
)


(NOT-UNDERSTOOD
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "( (unsupported-act INFORMA) 
            (unexpected-act PROPOSE)
            (unsupported-value content)
            (unrecognised-value ontology)

 )"
 :X-comment "a bit of not-understood exceptions are here"
)





(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((action (Agent-Identifier :name DF@IOR:001122)
            (GET-DESCRIPTION)
          ))"
 :X-comment ""
)

(AGREE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content " ((action (Agent-Identifier :name DF@IOR:001122)
            (GET-DESCRIPTION)
          ) true)"
 :X-comment ""
)

(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content " (( result (action (Agent-Identifier :name DF@IOR:001122)
                            (GET-DESCRIPTION))
           (AP-Description 
             :name JADE
             :dynamic true
             :mobility FALsE
             :transport-profile 
              (AP-Transport-Description
               :available-mtps (set
                (mtp-description :profile alpha :mtp-name IIOP
                 :addresses (sequence iiop://cselt.it:50/acc)))))
          ))"
 :X-comment "Inform of the result of the action"
)



(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content " ((action (Agent-Identifier :name DF@IOR:001122)
            (MODIFY 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
          ))) )"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)

(REQUEST
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content " ((action (Agent-Identifier :name DF@IOR:001122)
            (DEREGISTER
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233 :x-dummy (AID gg))
          ))) )"
 :X-comment "In the message you must put at a minimum all the mandatory slots, otherwise you get an exception."
)





(REQUEST
 :sender (Agent-Identifier :name x@ior:002233)
 :receiver (set (Agent-Identifier :name y) 
                (Agent-Identifier :name z :addresses (sequence iiop://100.1/a))
                (Agent-Identifier :name z :resolvers (sequence (Agent-Identifier :name p))))
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content " ((action (Agent-Identifier :name DF@IOR:001122)
            (REGISTER 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
             :services (set
               (Service-Description
                :name Service1
                :type TestType
                :ontologies (set O1 O2 O3)
                :protocols (set P1 P2 P3)
                :ownership CSELT
                :properties (set (Property :name X1 :value V1) 
          			 (Property :name X3 :value 12) 
          			 (Property :name X4 :value -12.23) 
          			 (Property :name X5 :value 20001211T100025100A)
          			 (Property :name X6 :value (Agent-Identifier :name x))
          			 (Property :name X7 :value (AGENT-identifier :name x))
                                 (Property :name X2 :value V2) )))
             :protocols (set FIPA-Request FIPA-Contract-Net)
             :ontologies (set FIPA-Agent-Management Trading)
             :languages (set FIPA-SL0)
          ))))"
 :X-comment "Request to register a Description with the DF"
)

(AGREE
 :sender (Agent-Identifier :name x@ior:002233)
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((action (Agent-Identifier :name DF@IOR:001122)
            (REGISTER 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
          ))) true )"
 :X-comment "In the agree message you must put all the mandatory slots, otherwise you get an exception."
)

(INFORM
 :sender (Agent-Identifier :name x@ior:002233)
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((done (action (Agent-Identifier :name DF@IOR:001122)
            (REGISTER 
             (Df-agent-description
             :name (Agent-Identifier :name x@ior:002233)
          ))) ))"
 :X-comment "In the message you must put all the mandatory slots, otherwise you get an exception."
)


(REQUEST
 :sender (Agent-Identifier :name x@ior:002233)
 :receiver (set (Agent-Identifier :name y))
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((action (Agent-Identifier :name AMS)
            (REGISTER 
             (Ams-agent-description
             :name (Agent-Identifier :name x@ior:002233 :X-userdef ciao)
             :ownership Test
             :state active
          ))))"
 :X-comment "Request the AMS to register an agent description"
)

(AGREE
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "((action (Agent-Identifier :name AMS)
            (REGISTER 
             (Ams-agent-description
             :name (Agent-Identifier :name x@ior:002233)
             :ownership Test
             :state active
          ))) true)"
 :X-mycomment Just_A_Test
 :X-myparam2 "0x23AF"
)

(INFORM
 :language FIPA-SL0
 :ontology fipa-agent-management
 :content "(
  (done (action (Agent-Identifier :name AMS)
            (REGISTER 
             (Ams-agent-description
             :name (Agent-Identifier :name x@ior:002233)
             :ownership Test
             :state active
          )))
 ))"
 :X-mycomment Just_A_Test
 :X-myparam2 "0x23AF"
)




