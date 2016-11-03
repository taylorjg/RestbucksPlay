package ops

object StringOps {
  implicit class StringOps(s: String) {
    def trim(c: Char): String = s.reverse.dropWhile(c => c == '/').reverse
  }
}
