
## Description

The idea of this repo is to implement the Restbucks domain application protocol (DAP) from the book, _REST in Practice: Hypermedia and Systems Architecture_, in Scala using Play and Akka. In particular, this implementation will be based on the section, _Building the Ordering Service in .NET_, in chapter 5.

## Planned Development Outline

This is a bit sketchy at the moment but I am planning something along these lines:

* ~~Initially, manually create a hardcoded graph of objects rather than write a parser for the DSL~~
    * ~~Objects in the graph: Accept, State, Link, Error, StateMachineTemplate~~
* ~~Create a Play application to host Restbucks~~
* ~~Use String Interpolating Routing DSL ([sird](https://www.playframework.com/documentation/2.5.x/ScalaSirdRouter)) to handle all the API web requests~~
* Get a basic happy path implementation working (_IN PROGRESS_)
* Write unit tests (_IN PROGRESS_)
* Add error handling
* Deploy to Heroku
* Write a Gatling script to exercise the API
* Write a UI to visualise the traffic
* Write a UI to summarise the transactions
* Change the implementation to use Akka actors / async action methods
* Write a parser for the _Restbucks hypermedia DSL_
    * Using [Parser Combinators API](http://www.scala-lang.org/api/2.11.8/scala-parser-combinators) ?

## Links

* [REST in Practice: Hypermedia and Systems Architecture](http://restinpractice.com/)
    * [source code](http://restinpractice.com/book/sourcecode.html)
        * [Service with Hypermedia DSL (.NET)
](http://restinpractice.com/book/sourcecode/ch05/Chapter5-DotNetOrderService.zip)
