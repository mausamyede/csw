package csw.services.config.server.commons

import java.util.regex.Pattern

/**
 * PathValidator is used to validate any requested path against invalid characters at an entry point of config service
 */
private[config] object PathValidator {

  private val invalidChars   = "!#<>$%&'@^`~+,;=\\s"
  private val invalidPattern = Pattern.compile(s"[$invalidChars]+")

  private val invalidCharsMessage: String = invalidChars
    .replace("\\s", " ")
    .map(char ⇒ s"{$char}")
    .mkString(",")

  /**
   * gets a message for presence of invalid characters in the file path
   *
   * @param path       String representation of path
   * @return           Message for presence of invalid characters
   */
  def message(path: String): String =
    s"Input file path '$path' contains invalid characters. Note, these characters $invalidCharsMessage are not allowed in file path"

  /**
   * validates string representation of path for the presence of unsupported characters in file path
   *
   * @param path       String representation of path
   * @return           True if the path does not contain any unsupported character, false otherwise
   */
  def isValid(path: String): Boolean = !invalidPattern.matcher(path).find()
}
