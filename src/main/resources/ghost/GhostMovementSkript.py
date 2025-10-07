#!C:\Users\derwo\AppData\Local\Programs\Python\Python311
# -*- coding: utf-8 -*-
#
#     W = Wand
#     L = Leer 
#     S = Snack -> treated same as Leer
#     G = Geist -> treated same as Leer
#     C = Huhn
#     M = SnackMan
#
#     W W W -> northwest_square, north_square, northeast_square
#     W   W -> west_square, east_square
#     W W W -> southwest_square, south_square, southeast_square
#
#     direction: the direction index in which the ghosts head is looking and last walked into
#     e.g. if the solution liste is [W,L, ,W,2] the ghost is walking in south direction and looking into south direction too
#
#     The direction in which the ghost is moving is defined in the wiki.
#     The ghost will always go after SnackMan if it can see him. Otherwise, it will try to hunt chicken.
#     If neither SnackMan nor chicken are visible, it will try to walk straight forward (and if this is not possible,
#     it chooses a random direction)
#
#    returns: [north_square, east_square, south_square, west_square, indexOfNextPosition]
import random

WALL = 'W'
EMPTY = 'L'
SNACK = 'S'
GHOST = 'G'
CHICKEN = 'C'
SNACKMAN = 'M'
INVALID = 'X'


def choose_next_square(squares_liste):
    """
    Determine the next square for the ghost to move.
    Priority:
    1. Follow SnackMan if visible.
    2. Move towards Chicken.
    3. Move in the current walking direction or a random direction.

    Args: squares_list (list): eight squares around the ghost and current direction index.

    Returns: list: Updated squares list and direction of movement.
    """
    northwest_square, north_square, northeast_square, east_square, southeast_square, south_square, southwest_square, west_square, direction = list(
        squares_liste)
    direction = int(direction)

    solution_liste = [north_square, east_square, south_square, west_square]

    # make sure you cannot walk into a wall
    solution_liste = eliminate_walls(solution_liste)
    # Ghosts and Snacks are treated as if they are empty squares
    solution_liste = mark_snack_and_ghost_as_empty(solution_liste)

    if seeing_snackman(solution_liste):
        return add_walking_direction(choose_target_square(solution_liste, SNACKMAN, direction))

    if all_squares_have_chicken(solution_liste):
        return add_walking_direction(choose_target_square(solution_liste, CHICKEN, direction))

    elif at_least_one_square_with_chicken(solution_liste):
        return add_walking_direction(choose_target_square(solution_liste, CHICKEN, direction))

    else:
        return add_walking_direction(choose_target_square(solution_liste, EMPTY, direction))

# --- Utility Functions ---

def eliminate_walls(squares):
    """Replace WALLs with INVALID to eliminate them as movement options."""
    return [INVALID if square == WALL else square for square in squares]


def mark_snack_and_ghost_as_empty(squares):
    """Treat GHOST and SNACK as EMPTY squares."""
    return [EMPTY if square in {GHOST, SNACK} else square for square in squares]


def all_squares_have_chicken(squares):
    """Check if all squares are either CHICKEN or INVALID."""
    return set(squares).issubset({CHICKEN, INVALID})


def at_least_one_square_with_chicken(squares):
    """Check if at least one square contains CHICKEN."""
    return CHICKEN in squares


def seeing_snackman(squares):
    """Check if SNACKMAN is visible in any square."""
    return SNACKMAN in squares


def choose_target_square(liste, toReplace, direction):
    """
     Choose a target square (e.g., SNACKMAN or CHICKEN).
     If the current walking direction is valid, prefer it.
     """
    # check if current walking direction is ok
    if liste[direction] == toReplace:
        liste[direction] = " "
        return liste

    # get list of indexes of things to replace
    indexes_to_choose_from = []
    for i in range(len(liste)):
        if liste[i] == toReplace:
            indexes_to_choose_from.append(i)

    last_step = get_last_step(direction)  # the last square as the sky direction on which the chicken stood
    if len(indexes_to_choose_from) > 1 and last_step in indexes_to_choose_from:
        indexes_to_choose_from.remove(last_step)  # delete the previous walking direction

    # replace random index
    zufall_index = random.choice(indexes_to_choose_from)
    liste[zufall_index] = " "
    return liste


def get_last_step(direction):
    """
    For example, if it looks in the direction of 0 (north).
    If it came from the south and should therefore not decide to run back to the south
    Args:
        direction: the direction in which the script ghost is looking
    Returns:
        the last step the script ghost took.
    """
    if direction == 0:
        return 2
    elif direction == 1:
        return 3
    elif direction == 2:
        return 0
    elif direction == 3:
        return 1


def add_walking_direction(liste):
    """
    Append the index of the first EMPTY square to the list, indicating the direction.
    """
    new_liste = [liste[0]] + liste[1:]
    first_empty_index = next((i for i, x in enumerate(new_liste) if x == " "), None)
    return int(first_empty_index)
