import numpy as np
from random import shuffle, randrange
from time import time
from numba import jit
import weave

N_ITER = 10

def next_permutation(L):
    '''
    Permute the list L in-place to generate the next lexicographic permutation.
    Return True if such a permutation exists, else return False.
    '''
     
    n = len(L)
 
    #------------------------------------------------------------
 
    # Step 1: find rightmost position i such that L[i] < L[i+1]
    i = n - 2
    while i >= 0 and L[i] >= L[i+1]:
        i -= 1
     
    if i == -1:
        return False
 
    #------------------------------------------------------------
 
    # Step 2: find rightmost position j to the right of i such that L[j] > L[i]
    j = i + 1
    while j < n and L[j] > L[i]:
        j += 1
    j -= 1
     
    #------------------------------------------------------------
 
    # Step 3: swap L[i] and L[j]
    L[i], L[j] = L[j], L[i]
     
    #------------------------------------------------------------
 
    # Step 4: reverse everything to the right of i
    left = i + 1
    right = n - 1
 
    while left < right:
        L[left], L[right] = L[right], L[left]
        left += 1
        right -= 1
             
    return True

def withDelay(cities, maximumTime):
	n = len(cities)
	if (n == 1):
		return [0], 0
	path = range(1, n)
	bestPath = []
	bestTime = 10**18
	while True:
		last = 0
		curTime = 0
		check = True
		for i in range(n-1):
			curTime = curTime + cities[last][path[i]]
			last = path[i]
			if (curTime > maximumTime[path[i]-1]):
				check = False
				break
		if check:
			if curTime < bestTime:
				bestPath = list(path)
				bestTime = curTime
		if not next_permutation(path):
			break
	if (len(bestPath) > 0):
		bestPath.insert(0, 0)
	return bestPath, bestTime

def memoize(f):
	memo = {}
	def helper(n, curLoc, bitWise, cities):
		if (curLoc, bitWise) not in memo:
			memo[(curLoc, bitWise)] = f(n, curLoc, bitWise, cities)
		return memo[(curLoc, bitWise)]
	return helper

def dp(n, curLoc, bitWise, cities):
	if bitWise == ((1<<n) - 1):
		return 0
	res = 10**18
	for i in range(1, n):
		if (bitWise & (1 << i)) == 0:
			nextLoc = bitWise | (1<<i)
			nextRes = cities[curLoc][i] + dp(n, i,nextLoc, cities)
			if (nextRes < res):
				res = nextRes
	return res

def trace(f, cities):
	path = [0]
	curBit = 1
	n = len(cities)
	for i in range(1, n):
		nextPlace = 0
		for j in range(1, n):
			if (curBit & (1<<j) == 0):
				nextBit = curBit | (1<<j)
				curVal = f(n, path[i-1], curBit, cities)
				nextVal = f(n, j, nextBit, cities)
				if (nextVal + cities[path[i-1]][j] == curVal):
					nextPlace = j
		path.append(nextPlace)
		curBit = curBit | (1 << nextPlace)
	return path


def triTueAlgo(cities):
	if len(cities) > 20:
		return algorithm(cities)
	realDp = memoize(dp)
	return trace(realDp, cities), realDp(len(cities), 0, 1, cities)

def algorithm(cities):
	best_order = range( len(cities) )
	best_length = calc_length(cities, best_order)
	for i in range(N_ITER):
		order =  range( len(cities) )
		length = calc_length(cities, order)
		changed = True
		while changed:
			changed = False
			for i in range(1, len(cities)):
				for j in range(i+1, len(cities)):
					new_order = list(order)
					new_order[i], new_order[j] = new_order[j], new_order[i]
					new_length = calc_length(cities, new_order)
					if (new_length < length):
						length = new_length
						order = list(new_order)
						changed = True
		if length < best_length:
			best_length = length
			best_order = order
	
	return best_order, best_length

def calc_length(cities, path):
	length = 0
	for i in range(1, len(path) ):
		length += cities[path[i-1]][path[i]]
	
	return length

