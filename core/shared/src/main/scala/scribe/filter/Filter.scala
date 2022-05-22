package scribe.filter

import scribe.LogRecord

/**
  * Filter for use in FilterBuilder, which is a LogModifier
  */
trait Filter {
  def matches[M](record: LogRecord[M]): Boolean

  def &&(that: Filter): Filter = AndFilters(List(this, that))
  def ||(that: Filter): Filter = OrFilters(List(this, that))
}