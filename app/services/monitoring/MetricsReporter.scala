package services.monitoring

import java.util.Date
import java.util.concurrent.{ConcurrentHashMap, TimeUnit}

import akka.actor.Actor.Receive
import kamon.{Kamon, metric, threadFactory}
import kamon.metric.{Counter, Gauge, Metric, Timer}
import services.monitoring.MetricsReporter.createTimers

import scala.annotation.tailrec
import scala.collection.mutable

trait MetricsTimer {
  def record(startTime: Date, endTime: Date): Unit
  def start(): MetricsStartedTimer
  def run[T](function: => T): T
}

trait MetricsStartedTimer {
  def stop(): Unit
}

trait ClassifiedMetricsCounter {
  def withClassifier(classifierValue: String): MetricsCounter
}
trait MetricsCounter {
  def increment(times: Long): Unit
  def increment(): Unit = increment(1L)
}
trait MetricsHistogram {
  def record(value: Long)
}
trait MetricsGauge {
  def update(value: Double): Unit
}
trait ClassifiedMetricsTimer {
  def withClassifier(classifierValue: String): MetricsTimer
}

object MetricsReporterUtils {

  // https://gist.github.com/sidharthkuruvila/3154845
 def camel2Snake(str: String): String = {
    @tailrec
    def camel2SnakeRec(s: String, output: String, lastUppercase: Boolean): String =
      if (s.isEmpty) output
      else {
        val c = if (s.head.isUpper && !lastUppercase) "_" + s.head.toLower else s.head.toLower
        camel2SnakeRec(s.tail, output + c, s.head.isUpper && !lastUppercase)
      }
    if (str.forall(_.isUpper)) str.map(_.toLower)
    else {
      camel2SnakeRec(str, "", true)
    }
  }

 def generateName(originalName: String): String = {
    val element = Thread.currentThread().getStackTrace()(3)
    val classSimpleName = element.getClassName.replaceAll(".*\\.", "")
    s"artakka__${camel2Snake(classSimpleName)}__${camel2Snake(originalName)}"
  }

 def createMetricsCounter(counter: Counter) = {
    new MetricsCounter() {
      override def increment(times: Long) = {
        counter.increment(times)
      }
    }
  }

  def createMetricsHistogram(histogram: metric.Histogram) = {
    new MetricsHistogram {
      override def record(value: Long): Unit = {
        if (value < 0) {
          println("negativeValueForHistogram", s"Unexpected usage, value = ${value}")
          Thread.sleep(1000)
          System.exit(1)
        }
        histogram.record(value)
      }
    }
  }

  def createMetricsGauge(gauge: Gauge): MetricsGauge = {
    new MetricsGauge {
      override def update(value: Double): Unit = {
        gauge.update(value)
      }
    }
  }

 def createMetricsTimer(timer: Timer) = {
    new MetricsTimer {
      override def start(): MetricsStartedTimer = {
        val started = timer.start()
        new MetricsStartedTimer {
          override def stop(): Unit = started.stop()
        }
      }

      override def run[T](function: => T): T = {
        val started = start()
        val result = function
        started.stop()
        result
      }

      override def record(startTime: Date, endTime: Date): Unit = {
        timer.record(endTime.getTime - startTime.getTime, TimeUnit.MILLISECONDS)
      }
    }
  }

}

object MetricsReporter {

  def createTimers(name: String, classifierName: String): ClassifiedMetricsTimer = {
    createTimers(Kamon.timer(MetricsReporterUtils.generateName(name)), classifierName)
  }

  def createTimers(timer: Metric.Timer, classifierName: String): ClassifiedMetricsTimer = {
    val timersMap = new ConcurrentHashMap[String, MetricsTimer]()
    new ClassifiedMetricsTimer() {
      override def withClassifier(classifierValue: String): MetricsTimer = {
        if (!timersMap.contains(classifierValue)) {
          timersMap.put(classifierValue, MetricsReporterUtils.createMetricsTimer(timer.withTag(classifierName, classifierValue)))
        }
        timersMap.get(classifierValue)
      }
    }
  }

  def createTimers(name: String, classifierName: String, classifierValues: List[String]): Map[String, MetricsTimer] = {
    val timer = Kamon.timer(MetricsReporterUtils.generateName(name))
    classifierValues.map(classifierValue => {
      classifierValue -> MetricsReporterUtils.createMetricsTimer(timer.withTag(classifierName, classifierValue))
    }).toMap
  }

  def createTimer(name: String): MetricsTimer = {
    MetricsReporterUtils.createMetricsTimer(Kamon.timer(MetricsReporterUtils.generateName(name)).withoutTags())
  }

  def createCounters(name: String, classifierName: String): ClassifiedMetricsCounter = {
    val counter = Kamon.counter(MetricsReporterUtils.generateName(name))
    val countersMap = new ConcurrentHashMap[String, MetricsCounter]()
    new ClassifiedMetricsCounter() {
      override def withClassifier(classifierValue: String): MetricsCounter = {
        if (!countersMap.contains(classifierValue)) {
          countersMap.put(classifierValue, MetricsReporterUtils.createMetricsCounter(counter.withTag(classifierName, classifierValue)))
        }
        countersMap.get(classifierValue)
      }
    }
  }

  def createCounter(name: String): MetricsCounter = {
    val counter = Kamon.counter(MetricsReporterUtils.generateName(name)).withoutTags()
    MetricsReporterUtils.createMetricsCounter(counter)
  }

  def createHistogram(name: String): MetricsHistogram = {
    val histogram: metric.Histogram = Kamon.histogram(MetricsReporterUtils.generateName(name)).withoutTags()
    MetricsReporterUtils.createMetricsHistogram(histogram)
  }

  def createGauge(name: String): MetricsGauge = {
    val gauge: Gauge = Kamon.gauge(MetricsReporterUtils.generateName(name)).withoutTags()
    MetricsReporterUtils.createMetricsGauge(gauge)
  }

}

object MetricsCollectingReceive {

  def apply(receive: Receive): Receive = {
    val counter = Kamon.counter(MetricsReporterUtils.generateName("receive"))
    val countersMap = new ConcurrentHashMap[String, MetricsCounter]()
    val classifiedCounter = new ClassifiedMetricsCounter() {
      override def withClassifier(classifierValue: String): MetricsCounter = {
        if (!countersMap.contains(classifierValue)) {
          countersMap.put(classifierValue, MetricsReporterUtils.createMetricsCounter(counter.withTag("messageClass", classifierValue)))
        }
        countersMap.get(classifierValue)
      }
    }

    val timers = createTimers(Kamon.timer(MetricsReporterUtils.generateName("receiveTime")), "messageClass")

    val res: Receive = {
      case x if receive.isDefinedAt(x) => {
        val classifierValue = x.getClass.getSimpleName
        classifiedCounter.withClassifier(classifierValue).increment()
        timers.withClassifier(classifierValue).run {
          receive(x)
        }
      }
    }
    res
  }
}
