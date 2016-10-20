package hypermedia

case class StateMachineTemplate(uriTemplate: String,
                                className: String,
                                mediaType: String,
                                relationsIn: String,
                                initialState: State,
                                states: Map[String, State],
                                finalStates: Seq[State])
