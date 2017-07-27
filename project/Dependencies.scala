import sbt._
import sbt.Keys._


/**
  * Created by rolfsd on 6/23/17.
  */
object Dependencies {
  def makeModule( org: String, artifactPrefix: String, version: String ): String => ModuleID = {
    (id: String) => org %% (artifactPrefix + "-" + id) % version
  }

  object omnibus {
    private val module = makeModule( "com.github.dmrolfs", "omnibus", "0.61-SNAPSHOT" )
    val commons = module( "commons" )
    val archetype = module( "archetype" )
    val akka = module( "akka" )
    val all = Seq( commons, akka, archetype )
    val builder = "com.github.dmrolfs" %% "shapeless-builder" % "1.0.0"
  }

  object cats {
    private val module = makeModule( "org.typelevel", "cats", "0.9.0" )
    val core = module( "core" )
    val kernal = module( "kernel" )
    val macros = module( "macros" )
    val free = module( "free" )
    val all = Seq( core, kernal, macros, free )
  }

  object monix {
    private val version = "2.3.0"
    private def module( id: String ): ModuleID = "io.monix" %% s"""monix${if (id.nonEmpty) "-" + id else "" }""" % version
    val core = module( "" )
    val cats = module( "cats" )
    val all = Seq( core, cats )
  }

  object time {
    val joda = "joda-time" % "joda-time" % "2.9.9"
    val jodaConvert = "org.joda" % "joda-convert" % "1.8.1"
    val scalaTime = "com.github.nscala-time" %% "nscala-time" % "2.16.0"
    val all = Seq( joda, jodaConvert, scalaTime )
  }

  object apachecommons {
    val math3 = "org.apache.commons" % "commons-math3" % "3.6.1" withSources() withJavadoc()
    val all = Seq( math3 )
  }

  object commons {
    val figaro = "com.cra.figaro" % "figaro_2.11" % "4.1.0.0"
    val bloomFilter = "com.github.alexandrnikitin" %% "bloom-filter" % "0.10.1" withSources() withJavadoc()
    val all = Seq( figaro, bloomFilter )
  }

  object log {
    val persistLogging = "com.persist" %% "persist-logging" % "1.3.2"
    val typesafe = "com.typesafe.scala-logging" %% "scala-logging" % "3.5.0"

    object logback {
      private def module( id: String ): ModuleID = "ch.qos.logback" % ("logback-"+id) % "1.2.2"
      val core = module( "core" )
      val classic = module( "classic" )
    }

    val slf4j = "org.slf4j" % "slf4j-api" % "1.7.25" intransitive
    val log4jOverSlf4j = "org.slf4j" % "log4j-over-slf4j" % "1.7.25"
    val all = Seq( typesafe, logback.core, logback.classic, slf4j, log4jOverSlf4j, persistLogging )
  }

  object metrics {
    private val module = makeModule( "io.dropwizard.metrics", "metrics", "3.2.2" )

    val core = module( "core" )
    val graphite = module( "graphite" )
    val metricsScala = "nl.grons" %% "metrics-scala" % "3.5.6_a2.4"
    val hdrhistogramReservoir = "org.mpierce.metrics.reservoir" % "hdrhistogram-metrics-reservoir" % "1.1.2"
    val hdrhistogram = "org.hdrhistogram" % "HdrHistogram" % "2.1.9"

    val all = Seq( core, graphite, metricsScala, hdrhistogramReservoir, hdrhistogram )
  }

  object lens {
    private val monocleModule = makeModule( "com.github.julien-truffaut", "monocle", "1.4.0" )
    val monocleCore = monocleModule( "core" )
    val monocleMacro = monocleModule( "macro" )
    val monocleLaw = monocleModule( "law" )

    private val gogglesModule = makeModule( "com.github.kenbot", "goggles", "1.0" )
    val gogglesDsl = gogglesModule( "dsl" )
    val gogglesMacros = gogglesModule( "macros" )

    val all = Seq( monocleCore, monocleMacro, gogglesDsl, gogglesMacros )
  }

  val shapeless = "com.chuusai" %% "shapeless" % "2.3.2" withSources() withJavadoc()
  val freestyle = "io.frees" %% "freestyle" % "0.3.0" withSources()

  val commonDependencies = {
    omnibus.all ++
    log.all ++
    cats.all ++
    monix.all ++
    time.all ++
    apachecommons.all ++
    commons.all ++
    lens.all ++
    Seq(
      shapeless,
      freestyle
    ) ++
    Scope.test(
      lens.monocleLaw
    )
  }

  val defaultDependencyOverrides = Set.empty[ModuleID]


  object Scope {
    def compile( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "compile" )
    def provided( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "provided" )
    def test( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "test" )
    def runtime( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "runtime" )
    def container( deps: ModuleID* ): Seq[ModuleID] = deps map ( _ % "container" )
  }
}
