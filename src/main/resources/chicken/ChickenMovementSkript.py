#!C:\Users\derwo\AppData\Local\Programs\Python\Python311
# -*- coding: utf-8 -*-
#
#     W = Wand 
#     L = Leer 
#     S = Snack 
#     G = Geist
#    SM = Snackman
#
#     W W W -> northwest_square, north_square, northeast_square
#     W   W -> west_square, east_square
#     W W W -> southwest_square, south_square, southeast_square
#
#     direction: the direction index in which the chickens head is looking and last walked into
#     e.g. if the solution liste is [W,G, ,W,2] the chicken is walking in south direction and looking into south direction too
#
#     The direction in which the chicken is moving is defined in the wiki. It moved according to the definition,
#     it prefers walking into the same direction it last walked into. If this is (due to the wiki definition) not possible,
#     it will randomly choose a walking direction (according to the wiki)
#
#    returns: [north_square, east_square, south_square, west_square, indexOfNextPosition]
import random


WALL = 'W'
CHICKEN = 'C'
EMPTY = 'L'
SNACK = 'S'
GHOST = 'G'
SNACKMAN = 'SM'
INVALID = 'X'


def choose_next_square(squares_liste):
    """
    Bestimmt das nächste Feld für Snackman basierend auf den Regeln.
    Args:
        squares_liste (list): Liste der Umgebung von Snackman und der aktuellen Richtung.
    Returns:
        list: Die aktualisierte Liste mit der neuen Richtung.
    """
    one_North_two_West_square, one_North_one_West_square, one_North_square, one_North_one_East_square, one_North_two_East_square = squares_liste[
                                                                                                                                   5:10]
    two_West_square, one_West_square, chickens_square, one_East_square, two_East_square = squares_liste[10:15]
    one_South_two_West_square, one_South_one_West_square, one_South_square, one_South_one_East_square, one_Soutn_two_East_square = squares_liste[
                                                                                                                                   15:20]
    solution_liste = [one_North_square, one_East_square, one_South_square, one_West_square]
    direction = int(squares_liste[len(squares_liste) - 1])

    # make sure you cannot walk into a wall
    solution_liste = eliminate_walls_as_options(solution_liste)
    solution_liste = eliminate_snackman_as_option(solution_liste)
    solution_liste = eliminate_chicken_as_option(solution_liste)

    # make sure you do not walk into a ghost
    if all_squares_have(solution_liste, GHOST):
        return add_walking_direction(choose_random_square(solution_liste, GHOST, direction))
    # choose square with snack
    if all_squares_have_snack(solution_liste):
        # choose random snack
        return add_walking_direction(choose_random_square(solution_liste, SNACK, direction))
    elif any_square_has(solution_liste, SNACK):
        # choose a square with snack (far away from ghost)
        return add_walking_direction(choose_snack_away_from_ghost(solution_liste, direction))
    elif any_square_has(solution_liste, GHOST):
        # choose square without snack, away from ghosts
        north, east, south, west = solution_liste
        return add_walking_direction(choose_square_without_snack_away_from_ghost(north, east, south, west, direction))
    else:
        # choose random square, no snacks there + no ghosts
        return add_walking_direction(choose_random_square(solution_liste, EMPTY, direction))

def getWaitingTime():
    return 2000

def eliminate_walls_as_options(squares):
    """
    Replaces walls with invalid squares.
    Args:
        squares (list): List of squares.
    Returns:
        list: Updated list where walls are replaced with invalid squares.
    """
    return [INVALID if sq == WALL else sq for sq in squares]


def eliminate_snackman_as_option(squares):
    """
    Replaces Snackman with empty squares.
    Args:
        squares (list): List of squares.
    Returns:
        list: Updated list where Snackman is replaced with empty squares.
    """
    return [EMPTY if sq == SNACKMAN else sq for sq in squares]


def eliminate_chicken_as_option(squares):
    """
    Replaces a chicken with empty squares.
    Args:
        squares (list): List of squares.
    Returns:
        list: Updated list where chicken is replaced with empty squares.
    """
    return [EMPTY if sq == CHICKEN else sq for sq in squares]


def all_squares_have(squares, target):
    """
    Checks if all squares contain the specified target.
    Args:
        squares (list): List of squares.
        target (str): Target to check for.
    Returns:
        bool: True if all squares contain the target, False otherwise.
    """
    return all(sq == target or sq == INVALID for sq in squares)


def any_square_has(squares, target):
    """
    Checks if at least one square contains the specified target.
    Args:
        squares (list): List of squares.
        target (str): Target to check for.
    Returns:
        bool: True if at least one square contains the target, False otherwise.
    """
    return target in squares


def choose_random_square(squares, toReplace, direction):
    """
    Selects a random square containing the specified target.
    Args:
        squares (list): List of squares.
        toReplace (str): Target to replace.
        direction (int): Current walking direction.
    Returns:
        list: Updated list with a random target square replaced.
    """
    # check if current walking direction is ok
    index = int(direction)
    if squares[index] == toReplace:
        squares[index] = " "
        return squares
    # get list of indexes of things to replace
    indexes_to_choose_from = []
    for i in range(len(squares)):
        if squares[i] == toReplace:
            indexes_to_choose_from.append(i)
    last_step = get_last_step(index)  # the last square as the sky direction on which the chicken stood
    if len(indexes_to_choose_from) > 1 and int(last_step) in indexes_to_choose_from:
        indexes_to_choose_from.remove(last_step)  # delete the previous walking direction
    # replace random index
    try:
        zufall_index = random.choice(indexes_to_choose_from)
    except (ValueError,IndexError):
        squares[last_step] = " "
        return squares
    squares[zufall_index] = " "
    return squares


def get_last_step(direction):
    """
    For example, if it looks in the direction of 0 (north).
    If it came from the south and should therefore not decide to run back to the south
    Args:
        direction: the direction in which the chicken is looking
    Returns:
        the last step the chicken took.
    """
    if direction == 0:
        return 2
    elif direction == 1:
        return 3
    elif direction == 2:
        return 0
    elif direction == 3:
        return 1


def add_walking_direction(original_liste):
    """
    Adds the walking direction to the square list.
    Args:
        original_liste (list): Original list of squares.
    Returns:
        list: Updated list including the walking direction.
    """
    new_liste = [original_liste[0]] + original_liste[1:]
    first_empty_index = next((i for i, x in enumerate(new_liste) if x == " "), None)
    return int(first_empty_index)


def all_squares_have_snack(original_liste):
    """
    Checks if all squares contain snacks.
    Args:
        original_liste (list): List of squares.
    Returns:
        bool: True if all squares contain snacks, False otherwise.
    """
    return set(original_liste).issubset({SNACK, INVALID})


def choose_snack_away_from_ghost(original_liste, direction):
    """
    Chooses a snack square away from ghosts.
    Args:
        original_liste (list): List of squares.
        direction (int): Current direction.
    Returns:
        list: Updated list after choosing a snack square.
    """
    north_square, east_square, south_square, west_square = original_liste
    # check if there are ghosts, if not -> choose snack
    if not any_square_has(original_liste, GHOST):
        if any_square_has(original_liste, SNACK):
            # choose snack
            return choose_random_square(original_liste, SNACK, direction)
        else:
            # choose empty space
            return choose_random_square(original_liste, EMPTY, direction)
    # look at everyone opposite the G, if S there: go there
    if original_liste[direction] == "S":
        original_liste[direction] = " "
        return original_liste
    if north_square == "G" and south_square == "S":
        return [north_square, east_square, " ", west_square]
    if north_square == "S" and south_square == "G":
        return [" ", east_square, south_square, west_square]
    if west_square == "G" and east_square == "S":
        return [north_square, " ", south_square, west_square]
    if west_square == "S" and east_square == "G":
        return [north_square, east_square, south_square, " "]
    # if ghost and snack are next to each other -> choose snack
    if north_square == "G" and (east_square == "S" or west_square == "S"):
        return choose_random_square(original_liste, SNACK, direction)
    if east_square == "G" and (north_square == "S" or south_square == "S"):
        return choose_random_square(original_liste, SNACK, direction)
    if south_square == "G" and (east_square == "S" or west_square == "S"):
        return choose_random_square(original_liste, SNACK, direction)
    if west_square == "G" and (north_square == "S" or south_square == "S"):
        return choose_random_square(original_liste, SNACK, direction)

    # if L there: continue looking for S opposite G
    return choose_square_without_snack_away_from_ghost(north_square, east_square, south_square, west_square, direction)

def getWaitingTime():
    return 2000

def choose_square_without_snack_away_from_ghost(north_square, east_square, south_square, west_square, direction):
    """
    Chooses an empty square away from ghosts.
    Args:
        north_square (str): Square to the north.
        east_square (str): Square to the east.
        south_square (str): Square to the south.
        west_square (str): Square to the west.
        direction (int): Current direction.
    Returns:
        list: Updated list after choosing a square.
    """
    if list([north_square, east_square, south_square, west_square])[direction] == "L":
        solution_list = list([north_square, east_square, south_square, west_square])
        solution_list[direction] = " "
        return solution_list
    if north_square == "G" and south_square == "L":
        return [north_square, east_square, " ", west_square]
    if north_square == "L" and south_square == "G":
        return [" ", east_square, south_square, west_square]
    if north_square == "G" and south_square == "L":
        return [north_square, east_square, " ", west_square]
    if north_square == "L" and south_square == "G":
        return [" ", east_square, south_square, west_square]
    # if nothing found: take any L
    return choose_random_square([north_square, east_square, south_square, west_square], "L", direction)
