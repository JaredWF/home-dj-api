package org.eclectek.dj

import org.specs2.mutable.Specification

class UtilSpec extends Specification {
	"Util" should {
		"return valid ids of length 6" in {
			Util.getBase36(6) must have length(6)
		}

		"return valid ids of length 10" in {
			Util.getBase36(6) must have length(6)
		}

		"return empty string for length <= 0" in {
			Util.getBase36(-1) must have length(0)
		}
	}
}