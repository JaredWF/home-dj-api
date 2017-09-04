package org.eclectek.dj

import scala.util.Random

object Util {
	private val base36chars = "0123456789abcdefghijklmnopqrstuvwxyz".toCharArray

	private val random = new Random()

	def getBase36(length: Int): String = length match {
		case 1 => base36chars(random.nextInt(36)).toString
		case i if (i > 1) => base36chars(random.nextInt(36)) + getBase36(length - 1)
		case _ => ""
	}
}