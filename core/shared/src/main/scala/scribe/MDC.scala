package scribe

object MDC {
  private val threadLocal: InheritableThreadLocal[MDC] = new InheritableThreadLocal[MDC] {
    override def initialValue(): MDC = new MDC(None)

    override def childValue(parentValue: MDC): MDC = new MDC(Option(parentValue))
  }
  def instance: MDC = threadLocal.get()

  def set(mdc: MDC): Unit = threadLocal.set(mdc)
  def contextualize[Return](mdc: MDC)(f: => Return): Return = {
    val previous = threadLocal.get()
    set(mdc)
    try {
      f
    } finally {
      set(previous)
    }
  }

  def map: Map[String, () => String] = instance.map
  def get(key: String): Option[String] = instance.get(key).map(_())
  def update(key: String, value: => String): Unit = instance(key) = value
  def contextualize[Return](key: String, value: => String)(f: => Return): Return = instance.contextualize(key, value)(f)
  def remove(key: String): Unit = instance.remove(key)
  def clear(): Unit = instance.clear()
}

class MDC(parent: Option[MDC]) {
  private var _map: Map[String, () => String] = Map.empty

  def map: Map[String, () => String] = _map

  def get(key: String): Option[() => String] = _map.get(key)

  def update(key: String, value: => String): Unit = _map = _map + (key -> (() => value))

  def contextualize[Return](key: String, value: => String)(f: => Return): Return = {
    update(key, value)
    try {
      f
    } finally{
      remove(key)
    }
  }

  def remove(key: String): Unit = _map = _map - key

  def clear(): Unit = _map = Map.empty
}