package org.codingteam.icfpc2017

object AppEntry extends App {

  private def run(): Unit = {
    args match {
      case Array("--test-map", mapFilePath) =>
        val m = GameMap.Map.fromJsonFile(mapFilePath)
        val map = GraphMap.fromMap(m)
        println(map)
        println(map.getMineNodes)
        // println(map.toGraph())

      case Array("--test-move-parse") =>
        //val moveStr = """{"claim":{"punter":0,"source":0,"target":1}}"""
        val moveStr = """{"pass":{"punter":1}}"""
        val move = Messages.parseMoveStr(moveStr)
        println(move)

      case Array("--test-parse", path) =>
        val message = Messages.parseServerMessageFile(path)
        println(message)

      case Array("--tcp", host, Parsing.I(port)) =>
        runTcpLoop(host, port)
      case _ =>
        println("Hello!")
    }

  }

  def runTcpLoop(host: String, port: Int): Unit = {
    HandlerLoop.runLoop(TcpInterface.connect(host, port), strategy, offline = false)
  }

  // TODO: implement real strategy.

  lazy val strategy = new DelegatingStrategy(Seq(new DummyStrategy()))

  run()
}
