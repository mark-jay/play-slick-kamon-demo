package services.monitoring

object FrequentEventsMetricsReporter {

  private val counters = MetricsReporter.createCounters("frequentEvents", "eventType")

  def increment(classifier: String, times: Long): Unit = {
    counters.withClassifier(classifier).increment(times)
  }

  def increment(classifier: String): Unit = {
    counters.withClassifier(classifier).increment()
  }
}
