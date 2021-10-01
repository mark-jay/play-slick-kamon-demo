package controllers.error_handling.exceptions

case class AuthorizationException(reason: String) extends ApplicationException {
  override def code(): String = "authError"
  override def description(): String = reason
}
