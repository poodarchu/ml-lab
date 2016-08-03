#!/usr/bin/python

from thinkbayes import Suite


class M_and_M(Suite):
	"""Map from hypothesis (A or B) to probability."""

	mix94 = dict(brown=30,
				 yellow=20,
				 red=20,
				 green=10,
				 orange=10,
				 tan=10,
				 blue=0)

	mix96 = dict(blue=24,
				 green=20,
				 orange=16,
				 yellow=14,
				 red=13,
				 brown=13,
				 tan=0)

	hypoA = dict(bag1=mix94, bag2=mix96)
	hypoB = dict(bag1=mix96, bag2=mix94)

	hypotheses = dict(A=hypoA, B=hypoB)

	def Likelihood(self, data, hypo):
		"""Computes the likelihood of the data under the hypothesis.
		hypo: string hypothesis (A or B)
		
		data: tuple of string bag, string color
		"""
		bag, color = data
		mix = self.hypotheses[hypo][bag]
		print '+++++++++++'
		print self.hypotheses 
		print '+++++++++++'
		like = mix[color]
		return like


def main():
	suite = M_and_M('AB')
	
	suite.Update(('bag1', 'yellow'))
	suite.Update(('bag2', 'green'))
	
	suite.Print()
#	print suite.hypotheses['A']['bag1']['brown']


if __name__ == '__main__':
	main()
