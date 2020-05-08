public class FindNArray {
	public static boolean find1(int value, int[] array) {
		for(int a = 0; a < array.length; a++)
			if (array[a] == value)
				return true;
		return false;
	}

	public static boolean find2(int value, int[][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				if (array[a][b] == value)
					return true;
		return false;
	}

	public static boolean find3(int value, int[][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					if (array[a][b][c] == value)
						return true;
		return false;
	}

	public static boolean find4(int value, int[][][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					for(int d = 0; d < array[0][0][0].length; d++)
						if (array[a][b][c][d] == value)
							return true;
		return false;
	}

	public static boolean find5(int value, int[][][][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					for(int d = 0; d < array[0][0][0].length; d++)
						for(int e = 0; e < array[0][0][0][0].length; e++)
							if (array[a][b][c][d][e] == value)
								return true;
		return false;
	}


	public static boolean find6(int value, int[][][][][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					for(int d = 0; d < array[0][0][0].length; d++)
						for(int e = 0; e < array[0][0][0][0].length; e++)
							for(int f = 0; f < array[0][0][0][0][0].length; f++)
								if (array[a][b][c][d][e][f] == value)
									return true;
		return false;
	}

	public static boolean find7(int value, int[][][][][][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					for(int d = 0; d < array[0][0][0].length; d++)
						for(int e = 0; e < array[0][0][0][0].length; e++)
							for(int f = 0; f < array[0][0][0][0][0].length; f++)
								for(int g = 0; g < array[0][0][0][0][0][0].length; g++)
									if (array[a][b][c][d][e][f][g] == value)
										return true;
		return false;
	}

	public static boolean find8(int value, int[][][][][][][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					for(int d = 0; d < array[0][0][0].length; d++)
						for(int e = 0; e < array[0][0][0][0].length; e++)
							for(int f = 0; f < array[0][0][0][0][0].length; f++)
								for(int g = 0; g < array[0][0][0][0][0][0].length; g++)
									for(int h = 0; h < array[0][0][0][0][0][0][0].length; h++)
										if (array[a][b][c][d][e][f][g][h] == value)
											return true;
		return false;
	}

	public static boolean find9(int value, int[][][][][][][][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					for(int d = 0; d < array[0][0][0].length; d++)
						for(int e = 0; e < array[0][0][0][0].length; e++)
							for(int f = 0; f < array[0][0][0][0][0].length; f++)
								for(int g = 0; g < array[0][0][0][0][0][0].length; g++)
									for(int h = 0; h < array[0][0][0][0][0][0][0].length; h++)
										for(int i = 0; i < array[0][0][0][0][0][0][0].length; i++)
											if (array[a][b][c][d][e][f][g][h][i] == value)
												return true;
		return false;
	}

	public static boolean find10(int value, int[][][][][][][][][][] array) {
		for(int a = 0; a < array.length; a++)
			for(int b = 0; b < array[0].length; b++)
				for(int c = 0; c < array[0][0].length; c++)
					for(int d = 0; d < array[0][0][0].length; d++)
						for(int e = 0; e < array[0][0][0][0].length; e++)
							for(int f = 0; f < array[0][0][0][0][0].length; f++)
								for(int g = 0; g < array[0][0][0][0][0][0].length; g++)
									for(int h = 0; h < array[0][0][0][0][0][0][0].length; h++)
										for(int i = 0; i < array[0][0][0][0][0][0][0].length; i++)
											for(int j = 0; j < array[0][0][0][0][0][0][0][0].length; j++)
												if (array[a][b][c][d][e][f][g][h][i][j] == value)
													return true;
		return false;
	}
}