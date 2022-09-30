

## Two’s-Complement Encodings， 二进制位取反是的数学意义是什么呢？
假设此处二进制的长度为4。一个二进制数字x和它的二进制位反转的数x'的和是B1111即，` x+x' = B1111 = D-1(十进制的-1) ` 因此 ` x' = -1-x `, 现在我有一个数a,先加上`-1` 然后再取反是什么结果呢? 由上面的等式可得 ` a' = -1-(a-1) = -a`。所以根据上面的结果就明白如何得到一个二进制数字的负数了，也就是先加上`-1`再取反。例如，设` a = B0110 = D6 `, 

Insight:
$$
-x = -x - 1 + 1
   =  -1 - (x-1)
	 =  (2^n – 1) - (x+(2^n – 1))  // since  -1 = (2^n – 1)
	 =  (1111) - (x+(1111))
	 = flippedBits(x + (1111))
$$

Algorithm: 

To convert bbb...b:  add 1111 then flip all the bits of the result

Practice: Convert (6) 
```
	先加 -1,即二进制的1111
	
	   0110 (6)
	 + 1111 (-1)
	 -----------
	   0101 
	   
	然后0，1反转
	 ! 0101 
	----------
	   1010 (6)
	  
	 结果1010就是十进制的-6
	 	
```
 
 以上就是课程中ALU计算`-x`的数学原理。 `x-y`的实现也是同样的原理，先对x取反	`r = -1-x`, 然后把上面的结果加上y即`r = r+y = -1-x+y`，最后再对上面的结果取反`r = -1-r = -1-(-1-x+y) = x-y`

课程的视频里介绍的是另外一种计算方法，但是视频里介绍的计算方法无法用来理解课程中ALU设计用到的算法。视频里介绍的计算方法如下。
 
## 另外一种计算一个数的负数的方法

Compute –x from x

Insight:

In Two’s complement Encodings， represent negative number $-x$ using the positive number $2^n - x$.

$$
code(–x) = (2^n – x)
= 1 + (2^n – 1) – x = 1 + (1111) – x
= 1 + flippedBits(x)
$$

Algorithm: 
To convert bbb...b: Flip all the bits and add 1 to the result

Practice: Convert (–6) 
```
 			1010 (–6) 
			0101 (flipped)
		+    1 
		----------------
			0110 (6)
```
因为一个二进制数加1的算法就是: Flip the bits from right to left, stopping the first time 0 is flipped to one. 所以一个数先反转再加1后，它右边第一个1（包括）后面的数字与原来的一样，但是它（不包括）前面的0，1是与原来的数字是相反的，所以可以得出一个非常简单的计算二进制负数的表示的方法即，**一个二进制数的负数，就是这个数二进制位右侧第一个1（不包括）前面的0，1全部反转。**