package org.codingteam.icfpc2017

import java.time.Instant

import org.codingteam.icfpc2017.Common.Punter
import org.codingteam.icfpc2017.onlinegamer.OneBotOnServerGamer

object AppEntry extends App with LogbackLogger {

  private def run(): Unit = {
    args match {
      case Array("--test-map", mapFilePath) =>
        val m = GameMap.Map.fromJsonFile(mapFilePath)
        val map = GraphMap.fromMap(m)
        logger.info(map.toString)
        logger.info(map.getMineNodes.toString)

      case Array("--test-move-parse") =>
        //val moveStr = """{"claim":{"punter":0,"source":0,"target":1}}"""
        val moveStr = """{"pass":{"punter":1}}"""
        val move = Messages.parseMoveStr(moveStr)
        println(move)

      case Array("--test-parse", path) =>
        val message = Messages.parseServerMessageFile(path)
        println(message)

      case Array("--duck", path) =>
        val m = GameMap.Map.fromJsonFile(path)
        val map = GraphMap.fromMap(m)
        val n1 = map.getMineNodes.head
        val n2 = n1.neighbors.head

        map.mark(n1.value, n2.value, Punter(666))
        println(map)
        println(map.getFreeEdges())
        println(map.getPunterSubgraph(Punter(666)))
        println(map.score(Punter(666)))

      case Array("--tcp", host, Parsing.I(port)) =>
        runTcpLoop(host, port, None, "codingpunter")

      case Array("--online-gamer", name, maps) =>
        val mapz = maps.split(',').toList
        new OneBotOnServerGamer().run(mapz, name)

      case Array("--tcp-with-strategy", host, Parsing.I(port), strategy) =>
        runTcpLoop(host, port, None, strategy)

      case Array("--tcp-with-log", host, Parsing.I(port), name) =>
        runTcpLoop(host, port, Some(s"logs/game-${Instant.now().toEpochMilli}.lson"), name)

      case Array("--offline") =>
        runOfflineLoop(None, "codingpunter")

      case Array("--offline-with-log", name) =>
        runOfflineLoop(Some(s"logs/game-${Instant.now().toEpochMilli}.lson"), "codingpunter")

      case _ =>
        logger.info("Hello!")
    }

  }

  def runTcpLoop(host: String, port: Int, log: Option[String], name: String): Unit = {
    val strategy = selectStrategy(name)
    HandlerLoop(log).runLoop(StreamParser.connect(host, port, log), strategy, name, offline = false)
  }

  def runOfflineLoop(log: Option[String], name: String): Unit = {
    val strategy = selectStrategy(name)
    HandlerLoop(log).runOfflineMove(StreamParser.connectToStdInOut(log), strategy, name)
  }

  def selectStrategy(name: String): Strategy = {
    name match {
      case "codingpunter-dumb-obstructor" => new DelegatingStrategy(Seq(new DumbObstructorStrategy()))
      case "random-codingpunter" => new DelegatingStrategy(Seq(new RandomConnectorStrategy()))
      case "codingpunter" => new DelegatingStrategy(Seq(new GreedyStrategy()))
      case "connector" => new DelegatingStrategy(Seq(new ComponentConnectorStrategy()))
      case "mixed" => new MixedStrategy(Seq(
        (2.0, new GreedyStrategy()),
        (1.0, new ComponentConnectorStrategy()),
        (1.0, new DumbObstructorStrategy()),
        (1.0, new RandomConnectorStrategy())))
      case _ =>
        val ex = new Exception("unsupported name: " + name)
        logger.trace("Exception", ex)
        throw ex
    }
  }

  // lazy val strategy = new DelegatingStrategy(Seq(new GreedyStrategy()))

  run()
}
