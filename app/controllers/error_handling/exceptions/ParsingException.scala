package controllers.error_handling.exceptions

case class ParsingException(reason: String = "Failed to parse request") extends ApplicationException {
  override def code(): String = "parseError"
  override def description(): String = reason
}
