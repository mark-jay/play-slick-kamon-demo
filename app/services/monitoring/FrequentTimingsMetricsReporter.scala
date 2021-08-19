package services.monitoring

import java.util.Date

object FrequentTimingsMetricsReporter {

  private val timers = MetricsReporter.createTimers("frequentTimings", "eventType")

  def record(classifier: String, startDate: Date, endDate: Date): Unit = {
    timers.withClassifier(classifier).record(startDate, endDate)
  }

  def record(classifier: String, startDate: Date): Unit = {
    timers.withClassifier(classifier).record(startDate, new Date())
  }
}
