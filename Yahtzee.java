/*
 * File: Yahtzee.java
 * ------------------
 * This program will eventually play the Yahtzee game.
 */

import java.util.Arrays;

import javax.swing.text.StyledEditorKit.ForegroundAction;

import acm.io.*;
import acm.program.*;
import acm.util.*;

public class Yahtzee extends GraphicsProgram implements YahtzeeConstants {

	public static void main(String[] args) {
		new Yahtzee().start(args);
	}

	public void run() {
		IODialog dialog = getDialog();
		nPlayers = dialog.readInt("Enter number of players");
		if (nPlayers > MAX_PLAYERS) {
			while (nPlayers > MAX_PLAYERS) {
				nPlayers = dialog
						.readInt("Please enter different number of players");
			}
		}
		playerNames = new String[nPlayers];
		for (int i = 1; i <= nPlayers; i++) {
			playerNames[i - 1] = dialog.readLine("Enter name for player " + i);
		}
		display = new YahtzeeDisplay(getGCanvas(), playerNames);
		playGame();
	}

	/* game process */
	private void playGame() {
		fillList();
		while (round > 0) {
			for (int i = 1; i <= nPlayers; i++) {
				rollText(i);
				playEachPlayer(i);
			}
			round--;
		}
		chechWin();
	}

	/* game play for each players */
	private void playEachPlayer(int i) {
		rollFirstTime(i);
		reRoll();
		checkCategory(i);
		fillUpperScore(i);
		fillLowerScore(i);
		fillTotal(i);
	}

	/* this method is used when player rolls dices for the first time */
	private void rollFirstTime(int i) {
		display.waitForPlayerToClickRoll(i);
		for (int j = 0; j < dices.length; j++) {
			int dice = rgen.nextInt(1, 6);
			dices[j] = dice;
		}
		display.displayDice(dices);
	}

	/*
	 * this method is used when player wants to re-Roll dices, and he has
	 * opportunity to change the rolled dices
	 */
	private void reRoll() {
		for (int j = 0; j < 2; j++) {
			reRollText();
			display.waitForPlayerToSelectDice();
			for (int k = 0; k < dices.length; k++) {
				if (display.isDieSelected(k) == true) {
					dices[k] = rgen.nextInt(1, 6);
				}
			}
			display.displayDice(dices);
		}
	}

	/*
	 * this method fills the matrix (nplayer)x(category) with (-1) because as we
	 * know default value of the matrix is 0 and we want to control if selected
	 * category is already used or not, and if it is player must not fill it
	 * again. but the upper_score, upper_bonus and lower_score value are still
	 * 0.
	 */
	private void fillList() {
		list = new int[nPlayers][N_CATEGORIES];
		for (int i = 0; i < nPlayers; i++) {
			for (int j = 0; j < N_CATEGORIES; j++) {
				list[i][j] = -1;
			}
			list[i][UPPER_SCORE - 1] = 0;
			list[i][UPPER_BONUS - 1] = 0;
			list[i][LOWER_SCORE - 1] = 0;
		}
	}

	/* this checks if the category is form one to six */
	private boolean checkCategoryOneToSix(int category) {
		boolean res = false;
		for (int i = 0; i < N_DICE; i++) {
			if (dices[i] == category) {
				res = true;
			}
		}
		return res;
	}

	/* it fills matrix, about category one to six */
	private void fillCategoryOneToSix(int i, int category) {
		if (checkCategoryOneToSix(category)) {
			int score = pointONEStoSIX(category);
			list[i - 1][category - 1] = score;
			display.updateScorecard(category, i, score);
		} else {
			list[i - 1][category - 1] = 0;
			display.updateScorecard(category, i, 0);
		}
	}

	/* this checks if the category is three of a kind of four of a kind */
	private boolean chechThreeOfAKindOrFourOfAKind(int category) {
		int count = 1;
		int compare;
		if (category == THREE_OF_A_KIND) {
			compare = 3;
		} else {
			compare = 4;
		}
		for (int i = 0; i < dices.length; i++) {
			for (int j = 0; j < dices.length; j++) {
				if (dices[i] == dices[j] && i != j) {
					count++;
					if (count >= compare) {
						return true;
					}
				}
			}
			count = 1;
		}
		return false;
	}

	/* checks if category is full house */
	private boolean isFullHouse() {
		boolean res1 = false;
		boolean res2 = false;
		Arrays.sort(dices);
		if (dices[0] == dices[1] && dices[1] == dices[2]
				&& dices[2] != dices[3] && dices[3] == dices[4]) {
			res1 = true;
		}
		if (dices[0] == dices[1] && dices[1] != dices[2]
				&& dices[2] == dices[3] && dices[3] == dices[4]) {
			res2 = true;
		}
		if (res1 == true || res2 == true) {
			return true;
		} else {
			return false;
		}
	}

	/* this method checks if category is small straight */
	private boolean isSmallStraight() {
		boolean res1 = false;
		boolean res2 = false;
		boolean res3 = false;

		Arrays.sort(dices);
		
		// if dices have 1-2-3-4
		for (int i = 1; i <= 4; i++) {
			if (i == dices[i - 1]) {
				res1 = true;
			} else {
				res1 = false;
				break;
			}
		}
		// if dices have 2-3-4-5
		for (int i = 2; i <= 5; i++) {
			if (contains(i)) {
				res2 = true;
			} else {
				res2 = false;
				break;
			}
		}
		// if dices have 3-4-5-6
		for (int i = 3; i <= 6; i++) {
			if (contains(i)) {
				res3 = true;
			} else {
				res3 = false;
				break;
			}
		}
		if (res1 == true || res2 == true || res3 == true) {
			return true;
		} else {
			return false;
		}
	}

	/*
	 * this method is used only in isSmallStraight()
	 * and it says if dices have (i)
	 */
	private boolean contains(int i) {
		for (int j = 0; j < dices.length; j++) {
			if (i == dices[j]) {
				return true;
			}
		}
		return false;
	}

	/* this method checks if the category is large Straight */
	private boolean isLargeStraight() {
		boolean res1 = false;
		boolean res2 = false;

		Arrays.sort(dices);
		// if dice has 1-2-3-4-5
		for (int i = 0; i < dices.length; i++) {
			if (i + 1 == dices[i]) {
				res1 = true;
			} else {
				res1 = false;
				break;
			}
		}
		// if dice has 2-3-4-5-6
		for (int i = 2; i <= dices.length + 1; i++) {
			if (i == dices[i - 2]) {
				res2 = true;
			} else {
				res2 = false;
				break;
			}
		}
		if (res1 == true || res2 == true) {
			return true;
		} else {
			return false;
		}
	}

	/* checks if category is yahzee */
	private boolean isYahzee() {
		boolean res = true;
		for (int i = 1; i < dices.length; i++) {
			if (dices[0] != dices[i]) {
				res = false;
				break;
			}
		}
		return res;
	}

	/* it checks categories */
	private void checkCategory(int i) {
		int score;
		selectCategoryText();
		int category = display.waitForPlayerToSelectCategory();

		if (list[i - 1][category - 1] != -1) {
			category = securingCategoryFiled(i, category);
		}
		if (category >= ONES && category <= SIXES) {
			fillCategoryOneToSix(i, category);
		} else if (category == THREE_OF_A_KIND) {
			if (chechThreeOfAKindOrFourOfAKind(category)) {
				score = pointThreeOrFourKindOrChance();
			} else {
				score = 0;
			}
			fillScores(i, category, score);
		} else if (category == FOUR_OF_A_KIND) {
			if (chechThreeOfAKindOrFourOfAKind(category)) {
				score = pointThreeOrFourKindOrChance();
			} else {
				score = 0;
			}
			fillScores(i, category, score);
		} else if (category == FULL_HOUSE) {
			if (isFullHouse()) {
				score = 25;
			} else {
				score = 0;
			}
			fillScores(i, category, score);
		} else if (category == SMALL_STRAIGHT) {
			if (isSmallStraight()) {
				score = 30;
			} else {
				score = 0;
			}
			fillScores(i, category, score);
		} else if (category == LARGE_STRAIGHT) {
			if (isLargeStraight()) {
				score = 40;
			} else {
				score = 0;
			}
			fillScores(i, category, score);
		} else if (category == YAHTZEE) {
			if (isYahzee()) {
				score = 50;
			} else {
				score = 0;
			}
			fillScores(i, category, score);
		} else if (category == CHANCE) {
			score = pointThreeOrFourKindOrChance();
			fillScores(i, category, score);
		}
	}

	/* dynamic update, it fills matrix */
	private void fillScores(int i, int category, int score) {
		list[i - 1][category - 1] = score;
		display.updateScorecard(category, i, score);
	}

	/* it helps us not to enter score in already filled filed */
	private int securingCategoryFiled(int i, int category) {
		while (list[i - 1][category - 1] != -1) {
			category = display.waitForPlayerToSelectCategory();
		}
		return category;
	}

	/* calculates the score if category is from one to six */
	private int pointONEStoSIX(int i) {
		int res = 0;
		for (int j = 0; j < dices.length; j++) {
			if (dices[j] == i) {
				res += i;
			}
		}
		return res;
	}

	/* calculates the score if category three or four of a kind or chance */
	private int pointThreeOrFourKindOrChance() {
		int res = 0;
		for (int i = 0; i < dices.length; i++) {
			res += dices[i];
		}
		return res;
	}

	/* fills upper score */
	private void fillUpperScore(int i) {
		int score = 0;
		for (int j = 0; j <= 5; j++) {
			if (list[i - 1][j] >= 0) {
				score += list[i - 1][j];
			}
		}
		if (score >= 63) {
			list[i - 1][UPPER_BONUS - 1] = 35;
			display.updateScorecard(UPPER_BONUS, i, 35);
		}
		fillScores(i, UPPER_SCORE, score);
	}

	/* fills lower score */
	private void fillLowerScore(int i) {
		int score = 0;
		for (int j = 8; j <= 14; j++) {
			if (list[i - 1][j] >= 0) {
				score += list[i - 1][j];
			}
		}
		fillScores(i, LOWER_SCORE, score);
	}

	/* fills total score */
	private void fillTotal(int i) {
		int totalScore = list[i - 1][UPPER_SCORE - 1]
				+ list[i - 1][UPPER_BONUS - 1] + list[i - 1][LOWER_SCORE - 1];
		fillScores(i, TOTAL, totalScore);
	}

	/* checks winner */
	private void chechWin() {
		int winner = 0;
		int player = 0;
		for (int i = 1; i <= nPlayers; i++) {
			if (winner <= list[i - 1][TOTAL - 1]) {
				winner = list[i - 1][TOTAL - 1];
				player = i;
			}
		}
		winText(player);
	}

	/* it is used when player rolls */
	private void rollText(int i) {
		display.printMessage(playerNames[i - 1]
				+ "'s turn! Click >>> Roll Dice <<< to roll the dice.");
	}

	/* it is used when player re-rolls */
	private void reRollText() {
		display.printMessage("Select the dice you wish to re-roll and click >>> Roll Again <<<.");
	}

	/* it is used when player selects category */
	private void selectCategoryText() {
		display.printMessage("Select a category for this roll.");
	}

	/* it is used when someone wins */
	private void winText(int i) {
		display.printMessage("Congretulations, " + playerNames[i - 1]
				+ " , you're the winner with a total score of "
				+ list[i - 1][TOTAL - 1] + ".");
	}

	/* Private instance variables */
	private int nPlayers;
	private String[] playerNames;
	private YahtzeeDisplay display;
	private RandomGenerator rgen = new RandomGenerator();
	private int round = N_SCORING_CATEGORIES;
	private int[] dices = new int[N_DICE];
	private int[][] list;
}