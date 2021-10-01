package controllers.error_handling.exceptions

trait ApplicationException extends Exception {
  def code(): String
  def description(): String
}
