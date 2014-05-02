import java.io.OutputStream

class DiffCalculation extends OutputStream {

  private var wasNewLine = false

  private val NEW_LINE_CODE: Int = '\n'.asInstanceOf[Int]
  private val PLUS_CODE: Int = '+'.asInstanceOf[Int]
  private val MINUS_CODE: Int = '-'.asInstanceOf[Int]

  var additions: Long = 0
  var deletions: Long = 0

  override def write(b: Int): Unit = {
    if (wasNewLine) {
      if (b == PLUS_CODE) {
        additions += 1
      } else if (b == MINUS_CODE) {
        deletions += 1
      }
    }
    if (b == NEW_LINE_CODE) {
      wasNewLine = true
    } else {
      wasNewLine = false
    }
  }

  def reset() = {
    additions = 0
    deletions = 0
  }
}
