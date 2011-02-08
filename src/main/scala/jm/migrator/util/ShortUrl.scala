package jm.migrator.util

import net.lag.configgy.Configgy

/**
 * Authod: Yuri Buyanov
 * Date: 2/8/11 5:26 PM
 */

object ShortUrlEncoder {
  val config = Configgy.config
  val alphabet = config.getString(
                   "mapping.short_url.alphabet",
                   "abcdefghijklmnopqrstuwxyzABCDEFGHIJKLMNOPQRSTUWXY01234567890")
  val blockSize = config.getInt("mapping.short_url.blockSize", 12)
  val minLength = config.getInt("mapping.short_url.minLength", 4)

  val mask = (1 << blockSize) - 1
  val mapping = (0 until blockSize).reverse

  def encode(n: Long) = (n & ~mask) | _encode(n & mask)

  def _encode(n: Long) = {
    var result = 0
    mapping.zipWithIndex foreach { case (i,b) =>
      if ((n & (1 << i)) > 0) result = result | (1 << b)
    }
    result
  }

  def enbase(x: Long, minLength: Int = minLength) = {
    val result = _enbase(x)
    val padding = alphabet.charAt(0).toString * (minLength - result.length)
    "%s%s".format(padding, result)
  }

  def _enbase(x: Long): String = {
    val n = alphabet.length
    if (x < n){
      alphabet.charAt(x.toInt).toString
    } else {
      _enbase(x/n) + alphabet.charAt((x % n).toInt)
    }
  }

  def encodeUrl(n: Long, minLength: Int = minLength) =
     enbase(encode(n), minLength)
}